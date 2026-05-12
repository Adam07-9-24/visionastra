let isRefreshingGlobal = false;

export const authManager = {
  isRefreshing: () => isRefreshingGlobal,
  startRefresh: () => {
    isRefreshingGlobal = true;
  },
  endRefresh: () => {
    isRefreshingGlobal = false;
  },
};
