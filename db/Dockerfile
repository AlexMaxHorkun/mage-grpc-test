FROM postgres:12
ENV POSTGRES_PASSWORD "12345aBc"
ENV POSTGRES_USER "admin"
ENV POSTGRES_DB "magento"
COPY schema.sql /docker-entrypoint-initdb.d/
EXPOSE 5432
