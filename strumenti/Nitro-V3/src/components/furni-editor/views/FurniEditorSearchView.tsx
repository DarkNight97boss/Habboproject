import { FC, useCallback, useEffect, useEffectEvent, useMemo, useState } from 'react';
import { LocalizeText } from '../../../api';
import { Button, Column, Flex, LayoutFurniIconImageView, Text } from '../../../common';
import { FurniItem } from '../../../hooks/furni-editor';

interface FurniEditorSearchViewProps
{
    items: FurniItem[];
    total: number;
    page: number;
    loading: boolean;
    onSearch: (query: string, type: string, page: number) => void;
    onSelect: (id: number) => void;
}

type SortField = 'id' | 'spriteId' | 'itemName' | 'publicName' | 'type' | 'interactionType';
type SortDir = 'asc' | 'desc';

const SortArrow: FC<{ field: SortField; active: SortField; dir: SortDir }> = ({ field, active, dir }) =>
{
    if(field !== active) return <span className="ml-0.5 opacity-30">↕</span>;

    return <span className="ml-0.5">{ dir === 'asc' ? '▲' : '▼' }</span>;
};

export const FurniEditorSearchView: FC<FurniEditorSearchViewProps> = props =>
{
    const { items, total, page, loading, onSearch, onSelect } = props;
    const [ query, setQuery ] = useState('');
    const [ typeFilter, setTypeFilter ] = useState('');
    const [ sortField, setSortField ] = useState<SortField>('id');
    const [ sortDir, setSortDir ] = useState<SortDir>('asc');

    const initialSearch = useEffectEvent(() => onSearch('', '', 1));

    useEffect(() =>
    {
        initialSearch();
    }, []);

    const handleSearch = useCallback(() =>
    {
        onSearch(query, typeFilter, 1);
    }, [ query, typeFilter, onSearch ]);

    const handleKeyDown = useCallback((e: React.KeyboardEvent) =>
    {
        if(e.key === 'Enter') handleSearch();
    }, [ handleSearch ]);

    const handleSort = useCallback((field: SortField) =>
    {
        setSortDir(prev => (sortField === field ? (prev === 'asc' ? 'desc' : 'asc') : 'asc'));
        setSortField(field);
    }, [ sortField ]);

    const handleTypeToggle = useCallback((type: string) =>
    {
        setTypeFilter(prev =>
        {
            const next = prev === type ? '' : type;

            onSearch(query, next, 1);

            return next;
        });
    }, [ query, onSearch ]);

    const sortedItems = useMemo(() =>
    {
        const sorted = [ ...items ];

        sorted.sort((a, b) =>
        {
            let va: string | number = a[sortField] ?? '';
            let vb: string | number = b[sortField] ?? '';

            if(typeof va === 'string') va = va.toLowerCase();
            if(typeof vb === 'string') vb = vb.toLowerCase();

            if(va < vb) return sortDir === 'asc' ? -1 : 1;
            if(va > vb) return sortDir === 'asc' ? 1 : -1;

            return 0;
        });

        return sorted;
    }, [ items, sortField, sortDir ]);

    const totalPages = Math.ceil(total / 20);

    return (
        <Column gap={ 1 } className="h-full">
            <Flex gap={ 1 } alignItems="end">
                <Column gap={ 0 } className="flex-1">
                    <Text small bold>{ LocalizeText('furnieditor.search.label') }</Text>
                    <input
                        type="text"
                        className="w-full px-2 py-1 text-xs leading-normal rounded-sm border border-[#ccc] min-h-[calc(1.5em+0.5rem+2px)]"
                        placeholder={ LocalizeText('furnieditor.search.placeholder') }
                        value={ query }
                        onChange={ e => setQuery(e.target.value) }
                        onKeyDown={ handleKeyDown }
                    />
                </Column>
                <Flex gap={ 1 }>
                    { [ '', 's', 'i' ].map(t => (
                        <button
                            key={ t || 'all' }
                            className={ `px-2 py-1 text-[11px] rounded border cursor-pointer transition-colors ${
                                typeFilter === t
                                    ? 'bg-[#1e7295] text-white border-[#1e7295]'
                                    : 'bg-white text-[#333] border-[#ccc] hover:bg-[#f0f0f0]'
                            }` }
                            onClick={ () => handleTypeToggle(t) }
                        >
                            { t === '' ? LocalizeText('furnieditor.search.all') : t === 's' ? LocalizeText('furnieditor.search.floor') : LocalizeText('furnieditor.search.wall') }
                        </button>
                    )) }
                </Flex>
                <Button variant="primary" disabled={ loading } onClick={ handleSearch }>
                    { loading ? '...' : LocalizeText('furnieditor.search.button') }
                </Button>
            </Flex>

            { total > 0 &&
                <Text small variant="gray" className="text-[10px]">
                    { LocalizeText('furnieditor.search.items_found', [ 'count' ], [ String(total) ]) }
                </Text>
            }

            <Column gap={ 0 } className="flex-1 overflow-auto border border-[#ccc] rounded bg-white">
                <table className="w-full text-xs">
                    <thead>
                        <tr className="bg-[#e8e8e8] sticky top-0 select-none">
                            <th className="px-1 py-1 text-center w-[50px]"></th>
                            <th className="px-2 py-1 text-left cursor-pointer hover:bg-[#ddd]" onClick={ () => handleSort('id') }>
                                { LocalizeText('furnieditor.col.id') }<SortArrow field="id" active={ sortField } dir={ sortDir } />
                            </th>
                            <th className="px-2 py-1 text-left cursor-pointer hover:bg-[#ddd]" onClick={ () => handleSort('spriteId') }>
                                { LocalizeText('furnieditor.col.sprite') }<SortArrow field="spriteId" active={ sortField } dir={ sortDir } />
                            </th>
                            <th className="px-2 py-1 text-left cursor-pointer hover:bg-[#ddd]" onClick={ () => handleSort('itemName') }>
                                { LocalizeText('furnieditor.col.name') }<SortArrow field="itemName" active={ sortField } dir={ sortDir } />
                            </th>
                            <th className="px-2 py-1 text-left cursor-pointer hover:bg-[#ddd]" onClick={ () => handleSort('publicName') }>
                                { LocalizeText('furnieditor.col.public_name') }<SortArrow field="publicName" active={ sortField } dir={ sortDir } />
                            </th>
                            <th className="px-2 py-1 text-center cursor-pointer hover:bg-[#ddd]" onClick={ () => handleSort('type') }>
                                { LocalizeText('furnieditor.col.type') }<SortArrow field="type" active={ sortField } dir={ sortDir } />
                            </th>
                            <th className="px-2 py-1 text-left cursor-pointer hover:bg-[#ddd]" onClick={ () => handleSort('interactionType') }>
                                { LocalizeText('furnieditor.col.interaction') }<SortArrow field="interactionType" active={ sortField } dir={ sortDir } />
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        { sortedItems.map(item => (
                            <tr
                                key={ item.id }
                                className="cursor-pointer hover:bg-[#d4edfa] border-b border-[#eee] transition-colors"
                                onClick={ () => onSelect(item.id) }
                            >
                                <td className="px-1 py-1 text-center">
                                    <LayoutFurniIconImageView productType={ item.type } productClassId={ item.spriteId } className="inline-block scale-125" />
                                </td>
                                <td className="px-2 py-1 font-mono">{ item.id }</td>
                                <td className="px-2 py-1 font-mono">{ item.spriteId }</td>
                                <td className="px-2 py-1 truncate max-w-[120px]" title={ item.itemName }>{ item.itemName }</td>
                                <td className="px-2 py-1 truncate max-w-[120px]" title={ item.publicName }>{ item.publicName }</td>
                                <td className="px-2 py-1 text-center">
                                    <span className={ `px-1.5 py-0.5 rounded text-white text-[10px] font-medium ${ item.type === 's' ? 'bg-[#1e7295]' : 'bg-[#6b7280]' }` }>
                                        { item.type === 's' ? LocalizeText('furnieditor.search.floor') : LocalizeText('furnieditor.search.wall') }
                                    </span>
                                </td>
                                <td className="px-2 py-1 text-[10px]">{ item.interactionType || '-' }</td>
                            </tr>
                        )) }
                        { items.length === 0 && !loading &&
                            <tr>
                                <td colSpan={ 7 } className="px-2 py-4 text-center text-[#999]">{ LocalizeText('furnieditor.search.no_items') }</td>
                            </tr>
                        }
                    </tbody>
                </table>
            </Column>

            { totalPages > 1 &&
                <Flex gap={ 1 } justifyContent="between" alignItems="center">
                    <Text small variant="gray">
                        { LocalizeText('furnieditor.search.page', [ 'page', 'total' ], [ String(page), String(totalPages) ]) }
                    </Text>
                    <Flex gap={ 1 }>
                        <Button
                            variant="secondary"
                            disabled={ page <= 1 }
                            onClick={ () => onSearch(query, typeFilter, page - 1) }
                        >
                            { LocalizeText('furnieditor.search.prev') }
                        </Button>
                        <Button
                            variant="secondary"
                            disabled={ page >= totalPages }
                            onClick={ () => onSearch(query, typeFilter, page + 1) }
                        >
                            { LocalizeText('furnieditor.search.next') }
                        </Button>
                    </Flex>
                </Flex>
            }
        </Column>
    );
};
