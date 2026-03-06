# 第二课堂学分系统前端

## 技术栈

- Vue 3
- Vue Router 4
- Axios
- Vite

## 启动方式

```bash
npm install
npm run dev
```

默认地址：`http://127.0.0.1:5173`

## 前后端分离说明

- 前端独立运行在 `5173` 端口
- `vite.config.js` 已配置开发代理：`/api -> http://127.0.0.1:8080`
- 登录后使用 JWT 访问后端管理员接口
