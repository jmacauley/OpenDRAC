#!/bin/bash
for i in `find . -name "RTRV-RTG-INFO.tl1" -print`
do
	cat $i
done

