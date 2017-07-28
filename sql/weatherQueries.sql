CREATE TABLE "weatherQueries" (
  "id" serial PRIMARY KEY,
  "resolution" varchar(20) NOT NULL,
  "startDate" date NOT NULL,
  "endDate" date NOT NULL
);
