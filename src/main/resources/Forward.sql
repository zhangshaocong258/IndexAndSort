
create table `questionf` (
    `id` int primary key auto_increment,
	  `url` varchar(200) not null,
    `title` varchar(200) not null,
    `description` text(500) not null,
    `quality` int not null,
    `tQuality` int not null,
    `keywords` varchar(200) not null,
    `TF` varchar(200) not null
);

create table `peoplef` (
    `id` int primary key auto_increment,
	  `url` varchar(200) not null,
    `title` varchar(200) not null,
    `description` text(500),
    `quality` int not null,
    `keywords` varchar(200) not null
);

create table `topicf` (
    `id` int primary key auto_increment,
	  `url` varchar(200) not null,
    `title` varchar(200) not null,
    `description` text(500),
    `quality` int not null,
    `keywords` varchar(200)
);

create table `collectionf` (
    `id` int primary key auto_increment,
	  `url` varchar(200) not null,
    `title` varchar(200) not null,
    `description` text(500),
    `quality` int not null,
    `keywords` varchar(200)
);

ALTER TABLE forward auto_increment=1;
