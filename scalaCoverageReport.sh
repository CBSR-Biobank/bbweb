#!/bin/bash
sbt clean coverage test
sbt coverageReport
