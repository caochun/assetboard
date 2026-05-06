import { useState, useCallback } from 'react';

export function usePagination(initialPageSize = 20) {
  const [page, setPage] = useState(0);
  const [pageSize] = useState(initialPageSize);

  const nextPage = useCallback(() => setPage((p) => p + 1), []);
  const prevPage = useCallback(() => setPage((p) => Math.max(0, p - 1)), []);
  const goToPage = useCallback((p: number) => setPage(p), []);

  return { page, pageSize, nextPage, prevPage, goToPage };
}
