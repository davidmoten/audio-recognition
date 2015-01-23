# audio-recognition
Matches audio to small vocabulary using fast fourier transforms and Mel Frequency Cepstral Coefficients (MFCCs).

Status: *pre-alpha*

About the algorithm
-------------------------
A wave file is processed with the following techniques:

* Pre-emphasis (emphasizes higher frequencies)
* Framing (chopping up the wav into frames of say 256 values with 156 overlap)
* Hamming windowing (enforce periodicity of signal so FFT behaves well)
* Fast Fourier Transform (FFT)
* Triangular Bandpass Filters using Mel frequencies
* Discrete Cosine Transform (DCT)

The above processing gives for each frame of 256 values an list of 13 decimal values (MFCCs). The first 
value is a function of the overall power of the signal during the frame and the rest describe 
the frequency spectrum.

To compare wave file A with wave file B we calculate the MFCCs for each then use FastDTW to measure the 
distance after warping between the two sets of MFCCs.


Development resources
------------------------
[Audio signal processing book](http://mirlab.org/jang/books/audiosignalprocessing/speechFeatureMfcc.asp?title=12-2%20MFCC)<br/>
[Sound pattern matching using FastFourier Transform in Windows Phone](http://developer.nokia.com/community/wiki/Sound_pattern_matching_using_Fast_Fourier_Transform_in_Windows_Phone)<br/>
[Mel Frequency Cepstral Coefficient (MFCC) tutorial](http://practicalcryptography.com/miscellaneous/machine-learning/guide-mel-frequency-cepstral-coefficients-mfccs/)

