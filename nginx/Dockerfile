# Reverse proxy image: stock nginx + our config baked in.
# Baking the config into the image (instead of a bind mount) means the stack also
# deploys correctly when Jenkins talks to a Docker daemon on a different filesystem.
FROM nginx:1.30-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
