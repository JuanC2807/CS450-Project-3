
DROP TABLE Rentals;
DROP TABLE Bikes;

CREATE TABLE Bikes (
    BikeID      VARCHAR2(10),
    Type        VARCHAR2(20) NOT NULL,
    Status      VARCHAR2(20) NOT NULL,
    Location    VARCHAR2(50) NOT NULL,
    HourlyRate  NUMBER(5,2) NOT NULL,
    CONSTRAINT pk_bikes PRIMARY KEY (BikeID),
    CONSTRAINT chk_bikes_status CHECK (Status IN ('Available', 'Rented'))
);

CREATE TABLE Rentals (
    BikeID      VARCHAR2(10),
    StudentID   VARCHAR2(12) NOT NULL,
    StartTime   TIMESTAMP NOT NULL,
    EndTime     TIMESTAMP,
    CONSTRAINT pk_rentals PRIMARY KEY (BikeID, StudentID, StartTime),
    CONSTRAINT fk_rentals_bike FOREIGN KEY (BikeID) REFERENCES Bikes(BikeID),
    CONSTRAINT chk_rentals_time CHECK (EndTime IS NULL OR EndTime >= StartTime)
);

INSERT INTO Bikes VALUES ('B101', 'Road',     'Available', 'Johnson Center',       4.50);
INSERT INTO Bikes VALUES ('B102', 'Mountain', 'Available', 'Fenwick Library',      5.00);
INSERT INTO Bikes VALUES ('B103', 'Electric', 'Rented',    'Engineering Building', 8.50);
INSERT INTO Bikes VALUES ('B104', 'Hybrid',   'Available', 'Student Union',        4.75);
INSERT INTO Bikes VALUES ('B105', 'Road',     'Rented',    'Aquia Building',       4.50);
INSERT INTO Bikes VALUES ('B106', 'Electric', 'Available', 'Innovation Hall',      8.00);
INSERT INTO Bikes VALUES ('B107', 'Mountain', 'Available', 'Exploratory Hall',     5.25);
INSERT INTO Bikes VALUES ('B108', 'Hybrid',   'Rented',    'Planetary Hall',       4.75);
INSERT INTO Bikes VALUES ('B109', 'Road',     'Available', 'Merten Hall',          4.25);
INSERT INTO Bikes VALUES ('B110', 'Electric', 'Available', 'Research Hall',        8.75);
INSERT INTO Bikes VALUES ('B111', 'Road',     'Available', 'Enterprise Hall',      4.60);
INSERT INTO Bikes VALUES ('B112', 'Mountain', 'Rented',    'Horizon Hall',         5.10);
INSERT INTO Bikes VALUES ('B113', 'Hybrid',   'Available', 'SUB I',                4.80);
INSERT INTO Bikes VALUES ('B114', 'Electric', 'Rented',    'Buchanan Hall',        8.25);
INSERT INTO Bikes VALUES ('B115', 'Road',     'Available', 'Music/Theater Building', 4.40);
INSERT INTO Bikes VALUES ('B116', 'Hybrid',   'Available', 'Recreation Center',    4.95);
INSERT INTO Bikes VALUES ('B117', 'Mountain', 'Available', 'Lecture Hall',         5.30);
INSERT INTO Bikes VALUES ('B118', 'Electric', 'Available', 'East Building',        8.60);
INSERT INTO Bikes VALUES ('B119', 'Road',     'Rented',    'West Building',        4.55);
INSERT INTO Bikes VALUES ('B120', 'Hybrid',   'Available', 'Science and Tech I',   4.85);

-- Active rentals
INSERT INTO Rentals VALUES ('B103', 'G01324567', TIMESTAMP '2026-04-10 09:15:00', NULL);
INSERT INTO Rentals VALUES ('B105', 'G01456789', TIMESTAMP '2026-04-09 14:00:00', NULL);
INSERT INTO Rentals VALUES ('B108', 'G01239876', TIMESTAMP '2026-04-10 11:30:00', NULL);
INSERT INTO Rentals VALUES ('B112', 'G01550001', TIMESTAMP '2026-04-10 08:45:00', NULL);
INSERT INTO Rentals VALUES ('B114', 'G01660002', TIMESTAMP '2026-04-11 12:20:00', NULL);
INSERT INTO Rentals VALUES ('B119', 'G01770003', TIMESTAMP '2026-04-11 15:10:00', NULL);

-- Past rentals
INSERT INTO Rentals VALUES ('B101', 'G01111222', TIMESTAMP '2026-04-01 08:00:00', TIMESTAMP '2026-04-01 10:15:00');
INSERT INTO Rentals VALUES ('B102', 'G01114567', TIMESTAMP '2026-04-02 13:30:00', TIMESTAMP '2026-04-02 15:00:00');
INSERT INTO Rentals VALUES ('B104', 'G01440011', TIMESTAMP '2026-04-03 09:45:00', TIMESTAMP '2026-04-03 11:10:00');
INSERT INTO Rentals VALUES ('B106', 'G01225555', TIMESTAMP '2026-04-04 16:00:00', TIMESTAMP '2026-04-04 17:20:00');
INSERT INTO Rentals VALUES ('B107', 'G01009988', TIMESTAMP '2026-04-05 12:10:00', TIMESTAMP '2026-04-05 13:40:00');
INSERT INTO Rentals VALUES ('B109', 'G01557777', TIMESTAMP '2026-04-06 10:00:00', TIMESTAMP '2026-04-06 12:30:00');
INSERT INTO Rentals VALUES ('B110', 'G01330044', TIMESTAMP '2026-04-07 15:15:00', TIMESTAMP '2026-04-07 16:45:00');
INSERT INTO Rentals VALUES ('B111', 'G01880004', TIMESTAMP '2026-04-07 09:00:00', TIMESTAMP '2026-04-07 10:30:00');
INSERT INTO Rentals VALUES ('B113', 'G01990005', TIMESTAMP '2026-04-08 14:10:00', TIMESTAMP '2026-04-08 16:00:00');
INSERT INTO Rentals VALUES ('B115', 'G01212121', TIMESTAMP '2026-04-08 11:00:00', TIMESTAMP '2026-04-08 12:05:00');
INSERT INTO Rentals VALUES ('B116', 'G01313131', TIMESTAMP '2026-04-09 10:00:00', TIMESTAMP '2026-04-09 11:40:00');
INSERT INTO Rentals VALUES ('B117', 'G01414141', TIMESTAMP '2026-04-09 13:15:00', TIMESTAMP '2026-04-09 14:45:00');
INSERT INTO Rentals VALUES ('B118', 'G01515151', TIMESTAMP '2026-04-10 17:00:00', TIMESTAMP '2026-04-10 18:20:00');
INSERT INTO Rentals VALUES ('B120', 'G01616161', TIMESTAMP '2026-04-10 09:35:00', TIMESTAMP '2026-04-10 11:00:00');

COMMIT;
