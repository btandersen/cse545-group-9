SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

DROP SCHEMA IF EXISTS `mydb` ;
CREATE SCHEMA IF NOT EXISTS `mydb` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci ;
USE `mydb` ;

-- -----------------------------------------------------
-- Table `mydb`.`Users`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `mydb`.`Users` ;

CREATE  TABLE IF NOT EXISTS `mydb`.`Users` (
  `uid` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `uname` VARCHAR(45) NOT NULL ,
  `fname` VARCHAR(45) NULL ,
  `lname` VARCHAR(45) NULL ,
  `email` VARCHAR(45) NULL ,
  `role` INT UNSIGNED NOT NULL DEFAULT 0 ,
  `dept` SET('HR','LS','IT','SP','RD','FN','GUEST','TEMP') NOT NULL DEFAULT 'TEMP' ,
  `attempts` INT UNSIGNED NOT NULL DEFAULT 0 ,
  `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `pwd` VARCHAR(45) NULL ,
  PRIMARY KEY (`uid`) ,
  UNIQUE INDEX `uname_UNIQUE` (`uname` ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Docs`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `mydb`.`Docs` ;

CREATE  TABLE IF NOT EXISTS `mydb`.`Docs` (
  `did` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `title` VARCHAR(45) NOT NULL ,
  `auth` VARCHAR(45) NOT NULL ,
  `dept` SET('HR','LS','IT','SP','RD','FN','GUEST','TEMP') NOT NULL ,
  `ouid` INT UNSIGNED NOT NULL ,
  `created` DATETIME NOT NULL ,
  `lastAccess` DATETIME NULL ,
  `lastMod` DATETIME NULL ,
  `filename` VARCHAR(45) NOT NULL ,
  `file` MEDIUMBLOB NOT NULL ,
  PRIMARY KEY (`did`) ,
  INDEX `ouid` (`ouid` ASC) ,
  UNIQUE INDEX `title_UNIQUE` (`title` ASC) ,
  UNIQUE INDEX `filename_UNIQUE` (`filename` ASC) ,
  CONSTRAINT `ouid`
    FOREIGN KEY (`ouid` )
    REFERENCES `mydb`.`Users` (`uid` )
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Shared`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `mydb`.`Shared` ;

CREATE  TABLE IF NOT EXISTS `mydb`.`Shared` (
  `sdid` INT UNSIGNED NOT NULL ,
  `suid` INT UNSIGNED NOT NULL ,
  `perm` CHAR NOT NULL ,
  PRIMARY KEY (`sdid`, `suid`, `perm`) ,
  INDEX `sdid` (`sdid` ASC) ,
  INDEX `suid` (`suid` ASC) ,
  CONSTRAINT `sdid`
    FOREIGN KEY (`sdid` )
    REFERENCES `mydb`.`Docs` (`did` )
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `suid`
    FOREIGN KEY (`suid` )
    REFERENCES `mydb`.`Users` (`uid` )
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Locked`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `mydb`.`Locked` ;

CREATE  TABLE IF NOT EXISTS `mydb`.`Locked` (
  `ldid` INT UNSIGNED NOT NULL ,
  `luid` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`ldid`) ,
  INDEX `ldid` (`ldid` ASC) ,
  INDEX `luid` (`luid` ASC) ,
  CONSTRAINT `ldid`
    FOREIGN KEY (`ldid` )
    REFERENCES `mydb`.`Docs` (`did` )
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `luid`
    FOREIGN KEY (`luid` )
    REFERENCES `mydb`.`Users` (`uid` )
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Log`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `mydb`.`Log` ;

CREATE  TABLE IF NOT EXISTS `mydb`.`Log` (
  `idLog` INT NOT NULL AUTO_INCREMENT ,
  `uname` VARCHAR(45) NULL ,
  `title` VARCHAR(45) NULL ,
  `action` VARCHAR(45) NULL ,
  `result` VARCHAR(45) NULL ,
  `time` DATETIME NULL ,
  PRIMARY KEY (`idLog`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`groups`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `mydb`.`groups` ;

CREATE  TABLE IF NOT EXISTS `mydb`.`groups` (
  `groupid` VARCHAR(20) NOT NULL ,
  `uname` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`groupid`, `uname`) ,
  INDEX `FK_UNAME` (`uname` ASC) ,
  CONSTRAINT `FK_UNAME`
    FOREIGN KEY (`uname` )
    REFERENCES `mydb`.`Users` (`uname` )
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
