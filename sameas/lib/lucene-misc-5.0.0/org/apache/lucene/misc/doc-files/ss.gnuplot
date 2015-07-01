#
# Copyright(c) 2015, Samsung Electronics Co., Ltd.
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#     * Redistributions of source code must retain the above copyright
#      notice, this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above copyright
#      notice, this list of conditions and the following disclaimer in the
#      documentation and/or other materials provided with the distribution.
#     * Neither the name of the <organization> nor the
#      names of its contributors may be used to endorse or promote products
#      derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
# ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
# ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#
# ####################################################################
#
# Instructions for generating SVG renderings of the functions 
# used in SweetSpotSimilarity
#
# ####################################################################
#
#
set terminal svg size 600,400 dynamic enhanced fname 'arial'  fsize 11 butt solid 
set key inside left top vertical Right noreverse enhanced autotitles box linetype -1 linewidth 1.000
#
# #######  BASELINE TF
#
set output 'ss.baselineTf.svg'
set title "SweetSpotSimilarity.baselineTf(x)"
set xrange [0:20]
set yrange [-1:8]
btf(base,min,x)=(x <= min) ? base : sqrt(x+(base**2)-min)
#
plot btf(0,0,x) ti "all defaults", \
     btf(1.5,0,x) ti "base=1.5", \
     btf(0,5,x) ti "min=5", \
     btf(1.5,5,x) ti "min=5, base=1.5"
#
# #######  HYPERBOLIC TF
#
set output 'ss.hyperbolicTf.svg'
set title "SweetSpotSimilarity.hyperbolcTf(x)"
set xrange [0:20]
set yrange [0:3]
htf(min,max,base,xoffset,x)=min+(max-min)/2*(((base**(x-xoffset)-base**-(x-xoffset))/(base**(x-xoffset)+base**-(x-xoffset)))+1)
#
plot htf(0,2,1.3,10,x) ti "all defaults", \
     htf(0,2,1.3,5,x) ti "xoffset=5", \
     htf(0,2,1.2,10,x) ti "base=1.2", \
     htf(0,1.5,1.3,10,x) ti "max=1.5"
#
# #######  LENGTH NORM
#
set key inside right top
set output 'ss.computeLengthNorm.svg'
set title "SweetSpotSimilarity.computeLengthNorm(t)"
set xrange [0:20]
set yrange [0:1.2]
set mxtics 5 
cln(min,max,steepness,x)=1/sqrt( steepness * (abs(x-min) + abs(x-max) - (max-min)) + 1 )
#
plot cln(1,1,0.5,x) ti "all defaults", \
     cln(1,1,0.2,x) ti "steepness=0.2", \
     cln(1,6,0.2,x) ti "max=6, steepness=0.2", \
     cln(3,5,0.5,x) ti "min=3, max=5"
