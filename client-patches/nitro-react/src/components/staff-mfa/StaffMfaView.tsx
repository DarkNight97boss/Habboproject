import { QRCodeSVG } from 'qrcode.react';
import { FC, KeyboardEvent, useEffect, useState } from 'react';
import { GetConnection, SendMessageComposer } from '../../api';
import { Button, Column, Flex, NitroCardContentView, NitroCardHeaderView, NitroCardView, Text } from '../../common';
import { useMessageEvent } from '../../hooks';
import { StaffMfaMessageConfiguration, StaffMfaRequiredEvent, StaffMfaResultEvent, StaffMfaStatusRequestComposer, StaffMfaVerifyComposer } from './messages';

export const StaffMfaView: FC<{}> = props =>
{
    const [ registered, setRegistered ] = useState(false);
    const [ isVisible, setIsVisible ] = useState(false);
    const [ enrolled, setEnrolled ] = useState(true);
    const [ secret, setSecret ] = useState<string>('');
    const [ otpauthUri, setOtpauthUri ] = useState<string>('');
    const [ code, setCode ] = useState<string>('');
    const [ message, setMessage ] = useState<string>('');
    const [ submitting, setSubmitting ] = useState(false);

    // 1) Register the custom MFA message headers BEFORE the listeners below run.
    useEffect(() =>
    {
        const connection = GetConnection();

        if(!connection) return;

        connection.registerMessages(new StaffMfaMessageConfiguration());

        setRegistered(true);
    }, []);

    // 2) Server asks us to show the popup.
    useMessageEvent<StaffMfaRequiredEvent>(StaffMfaRequiredEvent, event =>
    {
        const parser = event.getParser();

        setEnrolled(parser.enrolled);
        setSecret(parser.secret || '');
        setOtpauthUri(parser.otpauthUri || '');
        setCode('');
        setMessage('');
        setSubmitting(false);
        setIsVisible(true);
    });

    // 3) Result of a verification attempt.
    useMessageEvent<StaffMfaResultEvent>(StaffMfaResultEvent, event =>
    {
        const parser = event.getParser();

        setSubmitting(false);

        if(parser.success)
        {
            setCode('');
            setMessage('');
            setIsVisible(false);
        }
        else
        {
            setCode('');
            setMessage(parser.message || 'Invalid code. Please try again.');
        }
    });

    // 4) Once registered, proactively ask the server whether we still need MFA.
    // This closes the login-time race where the popup could arrive before the
    // handler above was registered.
    useEffect(() =>
    {
        if(!registered) return;

        SendMessageComposer(new StaffMfaStatusRequestComposer());
    }, [ registered ]);

    const submit = () =>
    {
        if(submitting) return;

        const trimmed = (code || '').trim();

        if(!/^\d{6}$/.test(trimmed))
        {
            setMessage('Enter the 6-digit code from your authenticator app.');
            return;
        }

        setSubmitting(true);
        setMessage('');
        SendMessageComposer(new StaffMfaVerifyComposer(trimmed));
    };

    const onKeyDown = (event: KeyboardEvent<HTMLInputElement>) =>
    {
        if(event.key === 'Enter') submit();
    };

    if(!isVisible) return null;

    return (
        <NitroCardView uniqueKey="staff-mfa" className="nitro-staff-mfa" theme="primary-slim">
            <NitroCardHeaderView headerText="Staff verification" onCloseClick={ () => setIsVisible(false) } />
            <NitroCardContentView className="text-black">
                <Column gap={ 2 }>
                    { !enrolled &&
                        <Column gap={ 1 }>
                            <Text bold>Set up Google Authenticator</Text>
                            <Text small>Scan this QR with Google Authenticator (or any TOTP app), then enter the 6-digit code below to finish enrollment.</Text>
                            { !!otpauthUri &&
                                <Flex alignItems="center" justifyContent="center" className="bg-white rounded p-2">
                                    <QRCodeSVG value={ otpauthUri } size={ 180 } level="M" includeMargin={ false } />
                                </Flex> }
                            <Text small className="text-muted">Can&apos;t scan? Enter this key manually:</Text>
                            <Flex alignItems="center" justifyContent="center" className="bg-muted rounded p-1">
                                <Text bold className="font-monospace" style={ { letterSpacing: '2px', wordBreak: 'break-all' } }>{ secret }</Text>
                            </Flex>
                        </Column> }
                    { enrolled &&
                        <Text>Enter the 6-digit code from your authenticator app to unlock staff powers.</Text> }
                    <input
                        autoFocus
                        type="text"
                        inputMode="numeric"
                        maxLength={ 6 }
                        className="form-control form-control-sm text-center"
                        placeholder="000000"
                        value={ code }
                        onChange={ event => setCode(event.target.value.replace(/\D/g, '').slice(0, 6)) }
                        onKeyDown={ onKeyDown } />
                    { !!message &&
                        <Text small className="text-danger">{ message }</Text> }
                    <Flex justifyContent="end" gap={ 1 }>
                        <Button variant="success" disabled={ submitting || code.length !== 6 } onClick={ submit }>
                            { submitting ? 'Verifying...' : 'Verify' }
                        </Button>
                    </Flex>
                </Column>
            </NitroCardContentView>
        </NitroCardView>
    );
}
