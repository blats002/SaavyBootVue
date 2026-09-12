import axios from 'axios';

const SERVER_URL = import.meta.env.VITE_SERVER_URL || 'http://localhost:8080';

export default {
    getReport(year = 2026) {
        return axios.get(`${SERVER_URL}/api/pnl/report`, { params: { year } }).then((res) => res.data);
    },

    downloadExcel(year = 2026) {
        return axios
            .get(`${SERVER_URL}/api/pnl/report/export/excel`, {
                params: { year },
                responseType: 'blob'
            })
            .then(async (response) => {
                if (response.data && response.data.type === 'application/json') {
                    const text = await response.data.text();
                    let errMsg = 'Failed to generate Excel report';
                    try {
                        const json = JSON.parse(text);
                        errMsg = json.message || errMsg;
                    } catch (e) {}
                    throw new Error(errMsg);
                }
                const blob = new Blob([response.data], {
                    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
                });
                const url = window.URL.createObjectURL(blob);
                const link = document.createElement('a');
                link.href = url;
                link.download = `PnL_Statement_${year}.xlsx`;
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
                window.URL.revokeObjectURL(url);
            });
    },

    downloadTemplate() {
        return axios.get(`${SERVER_URL}/api/pnl/template`, { responseType: 'blob' }).then((response) => {
            const blob = new Blob([response.data], { type: 'text/csv;charset=utf-8;' });
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', 'pnl_ingestion_template.csv');
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);
        });
    },

    downloadAccountTemplate() {
        return axios.get(`${SERVER_URL}/api/pnl-accounts/template`, { responseType: 'blob' }).then((response) => {
            const blob = new Blob([response.data], { type: 'text/csv;charset=utf-8;' });
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', 'pnl_accounts_template.csv');
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);
        });
    },

    uploadCsv(file) {
        const formData = new FormData();
        formData.append('file', file);
        return axios
            .post(`${SERVER_URL}/api/pnl/ingest/csv`, formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            })
            .then((res) => res.data);
    },

    uploadAccountCsv(file) {
        const formData = new FormData();
        formData.append('file', file);
        return axios
            .post(`${SERVER_URL}/api/pnl-accounts/ingest/csv`, formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            })
            .then((res) => res.data);
    },

    ingestBatch(payload) {
        return axios.post(`${SERVER_URL}/api/pnl/ingest/batch`, payload).then((res) => res.data);
    },

    getDrilldown(params) {
        return axios.get(`${SERVER_URL}/api/pnl/drilldown`, { params }).then((res) => res.data || []);
    }
};
