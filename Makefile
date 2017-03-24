
release: always
	rm -rf release 
	lein cljsbuild once release

always:
