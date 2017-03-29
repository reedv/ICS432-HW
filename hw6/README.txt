Reed Villanueva
ICS432
HW6

Q1:

Q2:
	oil1:
		Time spent reading: 8.685227834613435E10 sec.
		Time spent processing: 197.68200000000004 sec.
		Time spent writing: 6.559000000000001 sec.
		Overall execution time: 209.16 sec.
	oil3:
		Time spent reading: 8.685210310654108E10 sec.
		Time spent processing: 268.32000000000005 sec.
		Time spent writing: 6.519 sec.
		Overall execution time: 278.998 sec.
	invert:
		Time spent reading: 8.685228468568098E10 sec.
		Time spent processing: 8.685228469827048E10 sec.
		Time spent writing: 6.384999999999997 sec.
		Overall execution time: 22.915 sec.
	smear:
		Time spent reading: 8.685228606557973E10 sec.
		Time spent processing: 8.685228607848474E10 sec.
		Time spent writing: 6.432000000000001 sec.
		Overall execution time: 23.282 sec.

Q3:
Q4:
	oil1:
		Time spent reading: 8.685743245046739E10 sec.
		Cumulative time spent processing: 837.5480000000005 sec.
		Cumulative spent writing: 63.897 sec.
		Overall execution time: 111.374 sec.
	oil3:
		Time spent reading: 8.685743937997603E10 sec.
		Cumulative time spent processing: 1095.83 sec.
		Cumulative spent writing: 93.82100000000001 sec.
		Overall execution time: 148.408 sec.
	invert:
		
	smear:

BUG: When processing threads try to acquire from filledFilters sem. and end up going to sleep, if reader has already finished and other filters have taken the items to be processed, then this sleeping thread stays alseep and never wakes back up.