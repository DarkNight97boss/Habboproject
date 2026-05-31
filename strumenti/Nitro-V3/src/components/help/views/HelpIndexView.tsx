import { GetCfhStatusMessageComposer } from '@nitrots/nitro-renderer';
import { FC } from 'react';
import { DispatchUiEvent, GetConfigurationValue, LocalizeText, ReportState, ReportType, SendMessageComposer } from '../../../api';
import { Button, Text } from '../../../common';
import { GuideToolEvent } from '../../../events';
import { useHelp } from '../../../hooks';

export const HelpIndexView: FC<{}> = props =>
{
    const { setActiveReport = null } = useHelp();

    const onReportClick = () =>
    {
        setActiveReport(prevValue =>
        {
            const currentStep = ReportState.SELECT_USER;
            const reportType = ReportType.BULLY;

            return { ...prevValue, currentStep, reportType };
        });
    };

    // "Customer Support Center" — general support ticket (no specific user/chats).
    // Skips SELECT_USER + SELECT_CHATS and goes straight to SELECT_TOPICS so the player
    // can pick a topic and write a free-form message. reportedUserId=0 is supported
    // server-side by the new CallForHelpEvent handler (Asteria Core commit ebc603d9).
    const onSupportClick = () =>
    {
        setActiveReport(prevValue =>
        {
            return {
                ...prevValue,
                currentStep: ReportState.SELECT_TOPICS,
                reportType: ReportType.EMERGENCY,
                reportedUserId: 0,
                reportedChats: []
            };
        });
    };

    return (
        <>
            <div className="flex flex-col justify-center alignp-items-enter grow! gap-1">
                <Text fontSize={ 3 }>{ LocalizeText('help.main.frame.title') }</Text>
                <Text>{ LocalizeText('help.main.self.description') }</Text>
            </div>
            <div className="flex flex-col gap-1">
                <Button onClick={ onReportClick }>{ LocalizeText('help.main.bully.subtitle') }</Button>
                <Button disabled={ !GetConfigurationValue('guides.enabled') } onClick={ () => DispatchUiEvent(new GuideToolEvent(GuideToolEvent.CREATE_HELP_REQUEST)) }>{ LocalizeText('help.main.help.title') }</Button>
                <Button onClick={ onSupportClick }>{ LocalizeText('help.main.self.tips.title') }</Button>
            </div>
            <Button textColor="black" variant="link" onClick={ () => SendMessageComposer(new GetCfhStatusMessageComposer(false)) }>{ LocalizeText('help.main.my.sanction.status') }</Button>
        </>
    );
};
