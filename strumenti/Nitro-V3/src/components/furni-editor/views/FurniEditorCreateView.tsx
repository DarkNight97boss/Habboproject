import { FC, useCallback, useState } from 'react';
import { LocalizeText } from '../../../api';
import { Button, Column, Flex, Text } from '../../../common';

interface FurniEditorCreateViewProps
{
    interactions: string[];
    loading: boolean;
    onCreate: (fields: Record<string, unknown>) => Promise<number | null>;
    onCreated: (id: number) => void;
}

export const FurniEditorCreateView: FC<FurniEditorCreateViewProps> = props =>
{
    const { interactions, loading, onCreate, onCreated } = props;
    const [ success, setSuccess ] = useState<number | null>(null);

    const [ form, setForm ] = useState({
        itemName: '',
        publicName: '',
        spriteId: 0,
        type: 's' as 's' | 'i',
        width: 1,
        length: 1,
        stackHeight: 0,
        allowStack: true,
        allowSit: false,
        allowLay: false,
        allowWalk: false,
        allowGift: true,
        allowTrade: true,
        allowRecycle: true,
        allowMarketplaceSell: true,
        allowInventoryStack: true,
        interactionType: '',
        interactionModesCount: 1,
        customparams: '',
    });

    const setField = useCallback((key: string, value: unknown) =>
    {
        setForm(prev => ({ ...prev, [key]: value }));
        setSuccess(null);
    }, []);

    const handleCreate = useCallback(async () =>
    {
        if(!form.itemName || !form.publicName) return;

        const id = await onCreate(form);

        if(id)
        {
            setSuccess(id);
            setTimeout(() => onCreated(id), 1000);
        }
    }, [ form, onCreate, onCreated ]);

    const inputClass = 'form-control form-control-sm';
    const labelClass = 'text-[11px] font-bold text-[#333] mb-0';

    return (
        <Column gap={ 1 } className="h-full overflow-auto">
            { success &&
                <div className="bg-[#d4edda] border border-[#c3e6cb] rounded p-2 text-[#155724] text-xs">
                    { LocalizeText('nitro.furnieditor.create.success', [ 'id' ], [ success.toString() ]) }
                </div>
            }

            <div className="bg-white rounded border border-[#ccc] p-2">
                <Text small bold variant="primary" className="mb-1 block">{ LocalizeText('nitro.furnieditor.section.basic') }</Text>
                <div className="grid grid-cols-2 gap-2">
                    <div>
                        <label className={ labelClass }>{ LocalizeText('nitro.furnieditor.field.item_name') } *</label>
                        <input className={ inputClass } value={ form.itemName } onChange={ e => setField('itemName', e.target.value) } placeholder="my_custom_furni" />
                    </div>
                    <div>
                        <label className={ labelClass }>{ LocalizeText('nitro.furnieditor.field.public_name') } *</label>
                        <input className={ inputClass } value={ form.publicName } onChange={ e => setField('publicName', e.target.value) } placeholder={ LocalizeText('nitro.furnieditor.field.public_name.placeholder') } />
                    </div>
                    <div>
                        <label className={ labelClass }>{ LocalizeText('nitro.furnieditor.field.sprite_id') }</label>
                        <input type="number" className={ inputClass } value={ form.spriteId } onChange={ e => setField('spriteId', Number(e.target.value)) } />
                    </div>
                    <div>
                        <label className={ labelClass }>{ LocalizeText('nitro.furnieditor.field.type') }</label>
                        <select className="form-select form-select-sm" value={ form.type } onChange={ e => setField('type', e.target.value) }>
                            <option value="s">{ LocalizeText('nitro.furnieditor.type.floor') }</option>
                            <option value="i">{ LocalizeText('nitro.furnieditor.type.wall') }</option>
                        </select>
                    </div>
                </div>
            </div>

            <div className="bg-white rounded border border-[#ccc] p-2">
                <Text small bold variant="primary" className="mb-1 block">{ LocalizeText('nitro.furnieditor.section.dimensions') }</Text>
                <div className="grid grid-cols-3 gap-2">
                    <div>
                        <label className={ labelClass }>{ LocalizeText('nitro.furnieditor.field.width') }</label>
                        <input type="number" className={ inputClass } value={ form.width } onChange={ e => setField('width', Number(e.target.value)) } />
                    </div>
                    <div>
                        <label className={ labelClass }>{ LocalizeText('nitro.furnieditor.field.length') }</label>
                        <input type="number" className={ inputClass } value={ form.length } onChange={ e => setField('length', Number(e.target.value)) } />
                    </div>
                    <div>
                        <label className={ labelClass }>{ LocalizeText('nitro.furnieditor.field.stack_height') }</label>
                        <input type="number" step="0.01" className={ inputClass } value={ form.stackHeight } onChange={ e => setField('stackHeight', Number(e.target.value)) } />
                    </div>
                </div>
            </div>

            <div className="bg-white rounded border border-[#ccc] p-2">
                <Text small bold variant="primary" className="mb-1 block">{ LocalizeText('nitro.furnieditor.section.permissions') }</Text>
                <div className="grid grid-cols-3 gap-x-3 gap-y-1">
                    { [ 'allowStack', 'allowWalk', 'allowSit', 'allowLay', 'allowGift', 'allowTrade', 'allowRecycle', 'allowMarketplaceSell', 'allowInventoryStack' ].map(key => (
                        <label key={ key } className="flex items-center gap-1 text-[11px] cursor-pointer">
                            <input
                                type="checkbox"
                                className="form-check-input"
                                checked={ (form as any)[key] }
                                onChange={ e => setField(key, e.target.checked) }
                            />
                            { LocalizeText(`nitro.furnieditor.permission.${ key.replace('allow', '').toLowerCase() }`) }
                        </label>
                    )) }
                </div>
            </div>

            <div className="bg-white rounded border border-[#ccc] p-2">
                <Text small bold variant="primary" className="mb-1 block">{ LocalizeText('nitro.furnieditor.section.interaction') }</Text>
                <div className="grid grid-cols-3 gap-2">
                    <div className="col-span-2">
                        <label className={ labelClass }>{ LocalizeText('nitro.furnieditor.field.type') }</label>
                        <select className="form-select form-select-sm" value={ form.interactionType } onChange={ e => setField('interactionType', e.target.value) }>
                            <option value="">{ LocalizeText('nitro.furnieditor.interaction.none') }</option>
                            { interactions.map(i => (
                                <option key={ i } value={ i }>{ i }</option>
                            )) }
                        </select>
                    </div>
                    <div>
                        <label className={ labelClass }>{ LocalizeText('nitro.furnieditor.field.modes') }</label>
                        <input type="number" className={ inputClass } value={ form.interactionModesCount } onChange={ e => setField('interactionModesCount', Number(e.target.value)) } />
                    </div>
                </div>
                <div className="mt-1">
                    <label className={ labelClass }>{ LocalizeText('nitro.furnieditor.field.custom_params') }</label>
                    <input className={ inputClass } value={ form.customparams } onChange={ e => setField('customparams', e.target.value) } />
                </div>
            </div>

            <Flex className="mt-1">
                <Button variant="success" disabled={ loading || !form.itemName || !form.publicName } onClick={ handleCreate }>
                    { loading ? LocalizeText('nitro.furnieditor.creating') : LocalizeText('nitro.furnieditor.create') }
                </Button>
            </Flex>
        </Column>
    );
};
