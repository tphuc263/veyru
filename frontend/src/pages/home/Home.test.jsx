// @vitest-environment jsdom
import '@testing-library/jest-dom/vitest';
import {fireEvent, render, screen, waitFor} from '@testing-library/react';
import {MemoryRouter} from 'react-router-dom';
import {beforeEach, expect, it, vi} from 'vitest';
import Home from './Home.jsx';
import {getNewsfeed} from '../../services/newsfeedService';
import {getExploreFeed} from '../../services/searchService';
import {pageCache} from '../../utils/pageCache';

vi.mock('../../services/newsfeedService', () => ({getNewsfeed: vi.fn()}));
vi.mock('../../services/searchService', () => ({getExploreFeed: vi.fn()}));
vi.mock('../../components/features/PhotoCard.jsx', () => ({
    default: ({photoId}) => <div>post-{photoId}</div>
}));
vi.mock('../../components/features/PhotoModal.jsx', () => ({default: () => null}));
vi.mock('../../components/features/SuggestedUsers.jsx', () => ({default: () => null}));
vi.mock('../../components/common/Loader.jsx', () => ({Loader: () => <div>loading</div>}));

const post = id => ({id, type: 'PHOTO', imageUrl: `${id}.png`, createdAt: '2026-08-25T00:00:00Z'});

beforeEach(() => {
    vi.clearAllMocks();
    pageCache.clearAll();
});

it('uses the initial page cursor for load-more', async () => {
    getNewsfeed
        .mockResolvedValueOnce({items: [post('one')], nextCursor: 'cursor-1', hasMore: true})
        .mockResolvedValueOnce({items: [post('two')], nextCursor: null, hasMore: false});

    render(<MemoryRouter><Home /></MemoryRouter>);
    await screen.findByText('post-one');

    fireEvent.click(screen.getByRole('button', {name: /xem thêm bài viết/i}));

    await screen.findByText('post-two');
    expect(getNewsfeed).toHaveBeenNthCalledWith(1, undefined, 20);
    expect(getNewsfeed).toHaveBeenNthCalledWith(2, 'cursor-1', 20);
});

it('starts a fresh ranking session when the load-more cursor expires', async () => {
    getNewsfeed
        .mockResolvedValueOnce({items: [post('old')], nextCursor: 'expired', hasMore: true})
        .mockRejectedValueOnce({response: {data: {code: 'INVALID_REQUEST_VALUE'}}})
        .mockResolvedValueOnce({items: [post('fresh')], nextCursor: null, hasMore: false});

    render(<MemoryRouter><Home /></MemoryRouter>);
    await screen.findByText('post-old');
    fireEvent.click(screen.getByRole('button', {name: /xem thêm bài viết/i}));

    await waitFor(() => expect(getNewsfeed).toHaveBeenCalledTimes(3));
    expect(getNewsfeed).toHaveBeenNthCalledWith(3, undefined, 20);
    expect(await screen.findByText('post-fresh')).toBeInTheDocument();
    expect(screen.queryByText('post-old')).not.toBeInTheDocument();
});

it('falls back to explore feed when initial newsfeed is empty', async () => {
    getNewsfeed.mockResolvedValueOnce({items: [], nextCursor: null, hasMore: false});
    getExploreFeed.mockResolvedValueOnce({
        items: [post('explore-1')],
        page: 0,
        size: 20,
        totalElements: 1,
        totalPages: 1
    });

    render(<MemoryRouter><Home /></MemoryRouter>);

    expect(await screen.findByText('Gợi ý khám phá dành cho bạn')).toBeInTheDocument();
    expect(screen.getByText('post-explore-1')).toBeInTheDocument();
    expect(getExploreFeed).toHaveBeenCalledWith(0, 20);
});
