const API = axios.create({ baseURL: 'http://localhost:8080/api', timeout: 5000 });

API.interceptors.request.use(config => {
    const token = localStorage.getItem('classroom_token');
    if (token) config.headers['X-Auth-Token'] = token;
    return config;
});

API.interceptors.response.use(
    response => response,
    error => {
        if (error.response?.status === 401) {
            localStorage.removeItem('classroom_token');
            localStorage.removeItem('classroom_user');
            window.location.reload();
        }
        return Promise.reject(error);
    }
);
