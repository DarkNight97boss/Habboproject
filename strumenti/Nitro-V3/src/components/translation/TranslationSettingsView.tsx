import { AddLinkEventTracker, ILinkEventTracker, RemoveLinkEventTracker } from '@nitrots/nitro-renderer';
import { FC, useEffect, useState } from 'react';
import { LocalizeText } from '../../api';
import { NitroCardContentView, NitroCardHeaderView, NitroCardView, Text } from '../../common';
import { useTranslation } from '../../hooks';

export const TranslationSettingsView: FC<{}> = () =>
{
    const [ isVisible, setIsVisible ] = useState(false);
    const {
        settings,
        supportedLanguages = [],
        availableTextLocales = [],
        languagesLoading = false,
        localizationTextsLoading = false,
        lastIncomingLanguage = '',
        lastOutgoingLanguage = '',
        lastError = '',
        updateSettings,
        ensureSupportedLanguagesLoaded,
        getLanguageName
    } = useTranslation();

    useEffect(() =>
    {
        const linkTracker: ILinkEventTracker = {
            linkReceived: (url: string) =>
            {
                const parts = url.split('/');

                if(parts.length < 2) return;

                switch(parts[1])
                {
                    case 'show':
                        setIsVisible(true);
                        return;
                    case 'hide':
                        setIsVisible(false);
                        return;
                    case 'toggle':
                        setIsVisible(prevValue => !prevValue);
                        return;
                }
            },
            eventUrlPrefix: 'translation-settings/'
        };

        AddLinkEventTracker(linkTracker);

        return () => RemoveLinkEventTracker(linkTracker);
    }, []);

    useEffect(() =>
    {
        if(!isVisible) return;

        ensureSupportedLanguagesLoaded();
    }, [ ensureSupportedLanguagesLoaded, isVisible ]);

    if(!isVisible) return null;

    return (
        <NitroCardView className="translation-settings-window w-[360px]" theme="primary-slim" uniqueKey="translation-settings">
            <NitroCardHeaderView headerText={ LocalizeText('translation.settings.title') } onCloseClick={ () => setIsVisible(false) } />
            <NitroCardContentView className="flex flex-col gap-3 text-black">
                <div className="flex items-center gap-2">
                    <input checked={ settings.enabled } className="form-check-input" type="checkbox" onChange={ event => updateSettings({ enabled: event.target.checked }) } />
                    <Text>{ LocalizeText('translation.settings.enable') }</Text>
                </div>
                <div className="rounded border border-black/10 bg-black/5 p-2 text-[11px] leading-4">
                    { LocalizeText('translation.settings.help.prefix') } <strong>{ LocalizeText('translation.settings.help.original') }</strong> { LocalizeText('translation.settings.help.and') } <strong>{ LocalizeText('translation.settings.help.translate') }</strong>.
                </div>
                <div className="flex flex-col gap-2">
                    <Text bold>{ LocalizeText('translation.settings.interface_texts') }</Text>
                    <div className="flex flex-col gap-1">
                        <label className="flex flex-col gap-1 text-[12px]">
                            <span>{ LocalizeText('translation.settings.localized_text_pack') }</span>
                            <select
                                className="rounded border border-black/20 bg-white px-2 py-1"
                                disabled={ localizationTextsLoading }
                                value={ settings.uiTextLanguage || '' }
                                onChange={ event => updateSettings({ uiTextLanguage: event.target.value }) }>
                                <option value="">{ LocalizeText('translation.settings.default_gamedata') }</option>
                                { availableTextLocales.map(locale => <option key={ locale.code } value={ locale.code }>{ locale.name }</option>) }
                            </select>
                        </label>
                    </div>
                </div>
                <div className="flex flex-col gap-2">
                    <Text bold>{ LocalizeText('translation.settings.incoming_messages') }</Text>
                    <div className="flex flex-col gap-1">
                        <Text>{ LocalizeText('translation.settings.detected_language') }: { getLanguageName(lastIncomingLanguage) }</Text>
                        <label className="flex flex-col gap-1 text-[12px]">
                            <span>{ LocalizeText('translation.settings.translate_into') }</span>
                            <select
                                className="rounded border border-black/20 bg-white px-2 py-1"
                                disabled={ languagesLoading || !supportedLanguages.length }
                                value={ settings.incomingTargetLanguage }
                                onChange={ event => updateSettings({ incomingTargetLanguage: event.target.value }) }>
                                { supportedLanguages.map(language => <option key={ language.code } value={ language.code }>{ language.name }</option>) }
                            </select>
                        </label>
                    </div>
                </div>
                <div className="flex flex-col gap-2">
                    <Text bold>{ LocalizeText('translation.settings.outgoing_messages') }</Text>
                    <div className="flex flex-col gap-1">
                        <Text>{ LocalizeText('translation.settings.detected_writing_language') }: { getLanguageName(lastOutgoingLanguage) }</Text>
                        <label className="flex flex-col gap-1 text-[12px]">
                            <span>{ LocalizeText('translation.settings.send_text_as') }</span>
                            <select
                                className="rounded border border-black/20 bg-white px-2 py-1"
                                disabled={ languagesLoading || !supportedLanguages.length }
                                value={ settings.outgoingTargetLanguage }
                                onChange={ event => updateSettings({ outgoingTargetLanguage: event.target.value }) }>
                                { supportedLanguages.map(language => <option key={ language.code } value={ language.code }>{ language.name }</option>) }
                            </select>
                        </label>
                    </div>
                </div>
                <div className="flex items-center justify-between text-[11px] text-black/60">
                    <span>{ languagesLoading ? LocalizeText('translation.settings.loading_languages') : LocalizeText('translation.settings.languages_available', [ 'count' ], [ supportedLanguages.length.toString() ]) }</span>
                    <button className="rounded border border-black/15 bg-white px-2 py-1 text-[11px] text-black hover:bg-black/5" type="button" onClick={ () => ensureSupportedLanguagesLoaded(true) }>
                        { LocalizeText('translation.settings.refresh') }
                    </button>
                </div>
                { localizationTextsLoading &&
                    <div className="rounded border border-blue-300 bg-blue-50 px-2 py-1 text-[11px] leading-4 text-blue-700">
                        { LocalizeText('translation.settings.loading_localized_texts') }
                    </div> }
                { lastError.length > 0 &&
                    <div className="rounded border border-red-300 bg-red-50 px-2 py-1 text-[11px] leading-4 text-red-700">
                        { lastError }
                    </div> }
            </NitroCardContentView>
        </NitroCardView>
    );
};
