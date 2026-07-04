import axios from "axios";

const publicApi = axios.create({
  baseURL: "http://localhost:8083/api",
  headers: {
    "Content-Type": "application/json",
  },
});

export default publicApi;
