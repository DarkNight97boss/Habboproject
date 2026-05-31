import { FC } from 'react';
import { LocalizeText } from '../../api';
import { Button, Text } from '../../common';
import { VARIABLES_ELEMENTS } from './WiredCreatorTools.constants';
import { VariableDefinition, VariableTextValue } from './WiredCreatorTools.types';
import { useWiredCreatorToolsUiStore } from './wiredCreatorToolsUiStore';

export interface WiredVariablesTabViewProps
{
    variablePickerDefinitions: VariableDefinition[];
    selectedVariableDefinition: VariableDefinition | null;
    onPickVariable: (key: string) => void;
    canVariableHighlight: boolean;
    variableManageCanOpen: boolean;
    onOpenManagePanel: () => void;
    selectedVariableProperties: { key: string; value: string; }[];
    selectedVariableTextValues: VariableTextValue[];
}

/**
 * The "Variables" tab body of WiredCreatorToolsView. Extracted so the
 * parent module no longer carries 110 lines of inline JSX. Pure
 * presentation: every piece of state and every callback is supplied as
 * a prop, so this component is trivially memoizable and (eventually)
 * testable in isolation.
 */
export const WiredVariablesTabView: FC<WiredVariablesTabViewProps> = ({
    variablePickerDefinitions,
    selectedVariableDefinition,
    onPickVariable,
    canVariableHighlight,
    variableManageCanOpen,
    onOpenManagePanel,
    selectedVariableProperties,
    selectedVariableTextValues
}) =>
{
    const variablesType = useWiredCreatorToolsUiStore(s => s.variablesType);
    const setVariablesType = useWiredCreatorToolsUiStore(s => s.setVariablesType);
    const isVariableHighlightActive = useWiredCreatorToolsUiStore(s => s.isVariableHighlightActive);
    const setIsVariableHighlightActive = useWiredCreatorToolsUiStore(s => s.setIsVariableHighlightActive);

    return (
        <div className="p-3 min-h-[360px] flex gap-4">
            <div className="w-[205px] shrink-0 flex flex-col gap-3">
                <div className="flex flex-col gap-1">
                    <Text bold>{ LocalizeText('wired.tools.variables.type') }:</Text>
                    <div className="flex gap-1">
                        { VARIABLES_ELEMENTS.map(element => (
                            <button
                                key={ element.key }
                                type="button"
                                className={ `w-[42px] h-[38px] rounded border flex items-center justify-center shadow-[inset_0_1px_0_rgba(255,255,255,.7)] ${ element.disabled ? 'border-[#b7b7b7] bg-[#e7e3da] opacity-60 cursor-not-allowed' : ((variablesType === element.key) ? 'border-[#222] bg-[#d9d6cf]' : 'border-[#7f7f7f] bg-[#ece9e1]') }` }
                                disabled={ element.disabled }
                                onClick={ () => !element.disabled && setVariablesType(element.key) }
                                title={ element.label }>
                                <img alt={ element.label } className="w-auto h-auto max-w-[22px] max-h-[22px] object-contain" src={ element.icon } />
                            </button>
                        )) }
                    </div>
                </div>
                <div className="flex flex-col gap-1 min-h-0 grow">
                    <Text bold>{ LocalizeText('wired.tools.variables.picker') }:</Text>
                    <div className="grow rounded border border-[#bdb8ab] bg-white overflow-hidden">
                        <div className="max-h-[408px] overflow-y-auto">
                            <table className="w-full text-[12px]">
                                <tbody>
                                    { variablePickerDefinitions.map((variable, index) => (
                                        <tr
                                            key={ variable.key }
                                            className={ `cursor-pointer ${ (selectedVariableDefinition?.key === variable.key) ? 'bg-[#d7dfea]' : ((index % 2 === 0) ? 'bg-white' : 'bg-[#f3f3f3]') } hover:bg-[#e8eefc]` }
                                            onClick={ () => onPickVariable(variable.key) }>
                                            <td className="px-3 py-1 text-[#444]">{ variable.key }</td>
                                        </tr>
                                    )) }
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
                <div className="flex gap-2">
                    <Button
                        disabled={ !canVariableHighlight }
                        variant="secondary"
                        onClick={ () => setIsVariableHighlightActive(value => !value) }>
                        { isVariableHighlightActive ? LocalizeText('wired.tools.variables.undo') : LocalizeText('wired.tools.variables.highlight') }
                    </Button>
                    <Button
                        disabled={ !variableManageCanOpen }
                        variant="secondary"
                        onClick={ onOpenManagePanel }>
                        { LocalizeText('wired.tools.variables.manage') }
                    </Button>
                </div>
            </div>
            <div className="min-w-0 grow flex flex-col gap-3">
                { (variablesType === 'context') &&
                    <div className="rounded border border-[#c8b98f] bg-[#fff7df] px-3 py-2 text-[12px] text-[#6a5d33]">
                        { LocalizeText('wired.tools.variables.context.help') }
                    </div> }
                <div className="flex flex-col gap-1">
                    <Text bold>{ LocalizeText('wired.tools.variables.properties') }:</Text>
                    <div className="rounded border border-[#bdb8ab] bg-white overflow-hidden">
                        <div className="grid grid-cols-[1fr_120px] border-b border-[#d8d4c8] bg-[#f5f2ea] px-3 py-2 text-[12px] font-bold text-[#333]">
                            <span>{ LocalizeText('wired.tools.column.property') }</span>
                            <span>{ LocalizeText('wired.tools.column.value') }</span>
                        </div>
                        <div className="max-h-[210px] overflow-y-auto">
                            <table className="w-full text-[12px]">
                                <tbody>
                                    { selectedVariableProperties.map((property, index) => (
                                        <tr key={ property.key } className={ (index % 2 === 0) ? 'bg-white' : 'bg-[#f3f3f3]' }>
                                            <td className="px-3 py-1 text-[#444]">{ property.key }</td>
                                            <td className="px-3 py-1 text-[#222]">{ property.value }</td>
                                        </tr>
                                    )) }
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
                <div className="flex flex-col gap-1 min-h-0 grow">
                    <Text bold>{ LocalizeText('wired.tools.variables.text_values') }:</Text>
                    <div className="grow rounded border border-[#bdb8ab] bg-white overflow-hidden">
                        <div className="grid grid-cols-[120px_1fr] border-b border-[#d8d4c8] bg-[#f5f2ea] px-3 py-2 text-[12px] font-bold text-[#333]">
                            <span>{ LocalizeText('wired.tools.column.value') }</span>
                            <span>{ LocalizeText('wired.tools.column.text') }</span>
                        </div>
                        { !selectedVariableTextValues.length &&
                            <div className="h-[calc(100%-37px)] flex items-center justify-center text-[#b1aca2] text-[20px]">
                                <Text>{ LocalizeText('wired.tools.nothing_to_display') }</Text>
                            </div> }
                        { !!selectedVariableTextValues.length &&
                            <div className="max-h-[178px] overflow-y-auto">
                                <table className="w-full text-[12px]">
                                    <tbody>
                                        { selectedVariableTextValues.map((entry, index) => (
                                            <tr key={ `${ entry.value }-${ index }` } className={ (index % 2 === 0) ? 'bg-white' : 'bg-[#f3f3f3]' }>
                                                <td className="px-3 py-1 text-[#444]">{ entry.value }</td>
                                                <td className="px-3 py-1 text-[#222]">{ entry.text }</td>
                                            </tr>
                                        )) }
                                    </tbody>
                                </table>
                            </div> }
                    </div>
                </div>
            </div>
        </div>
    );
};
