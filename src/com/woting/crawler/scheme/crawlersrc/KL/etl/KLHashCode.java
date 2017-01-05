package com.woting.crawler.scheme.crawlersrc.KL.etl;

public class KLHashCode {

	 public static String decodeKL(String srcStr) {
	        int[] seed16={0,4129,8258,12387,16516,20645,24774,28903,33032,37161,41290,45419,49548,53677,57806,61935,4657,528,12915,8786,21173,17044,29431,25302,37689,33560,45947,41818,54205,50076
	                ,62463,58334,9314,13379,1056,5121,25830,29895,17572,21637,42346,46411,34088,38153,58862,62927,50604,54669,13907,9842,5649,1584,30423,26358,22165,18100,46939,42874,38681,34616,63455
	                ,59390,55197,51132,18628,22757,26758,30887,2112,6241,10242,14371,51660,55789,59790,63919,35144,39273,43274,47403,23285,19156,31415,27286,6769,2640,14899,10770,56317,52188,64447,60318
	                ,39801,35672,47931,43802,27814,31879,19684,23749,11298,15363,3168,7233,60846,64911,52716,56781,44330,48395,36200,40265,32407,28342,24277,20212,15891,11826,7761,3696,65439,61374,57309
	                ,53244,48923,44858,40793,36728,37256,33193,45514,41451,53516,49453,61774,57711,4224,161,12482,8419,20484,16421,28742,24679,33721,37784,41979,46042,49981,54044,58239,62302,689,4752
	                ,8947,13010,16949,21012,25207,29270,46570,42443,38312,34185,62830,58703,54572,50445,13538,9411,5280,1153,29798,25671,21540,17413,42971,47098,34713,38840,59231,63358,50973,55100,9939
	                ,14066,1681,5808,26199,30326,17941,22068,55628,51565,63758,59695,39368,35305,47498,43435,22596,18533,30726,26663,6336,2273,14466,10403,52093,56156,60223,64286,35833,39896,43963,48026
	                ,19061,23124,27191,31254,2801,6864,10931,14994,64814,60687,56684,52557,48554,44427,40424,36297,31782,27655,23652,19525,15522,11395,7392,3265,61215,65342,53085,57212,44955,49082,36825
	                ,40952,28183,32310,20053,24180,11923,16050,3793,7920};
	        long[] seed32={0l,1996959894l,3993919788l,2567524794l,124634137l,1886057615l,3915621685l,2657392035l,249268274l,2044508324l,3772115230l,2547177864l,162941995l,2125561021l,3887607047l,2428444049l,498536548l,1789927666l
	                ,4089016648l,2227061214l,450548861l,1843258603l,4107580753l,2211677639l,325883990l,1684777152l,4251122042l,2321926636l,335633487l,1661365465l,4195302755l,2366115317l,997073096l,1281953886l,3579855332l,2724688242l
	                ,1006888145l,1258607687l,3524101629l,2768942443l,901097722l,1119000684l,3686517206l,2898065728l,853044451l,1172266101l,3705015759l,2882616665l,651767980l,1373503546l,3369554304l,3218104598l,565507253l,1454621731l
	                ,3485111705l,3099436303l,671266974l,1594198024l,3322730930l,2970347812l,795835527l,1483230225l,3244367275l,3060149565l,1994146192l,31158534l,2563907772l,4023717930l,1907459465l,112637215l,2680153253l,3904427059l
	                ,2013776290l,251722036l,2517215374l,3775830040l,2137656763l,141376813l,2439277719l,3865271297l,1802195444l,476864866l,2238001368l,4066508878l,1812370925l,453092731l,2181625025l,4111451223l,1706088902l,314042704l
	                ,2344532202l,4240017532l,1658658271l,366619977l,2362670323l,4224994405l,1303535960l,984961486l,2747007092l,3569037538l,1256170817l,1037604311l,2765210733l,3554079995l,1131014506l,879679996l,2909243462l,3663771856l
	                ,1141124467l,855842277l,2852801631l,3708648649l,1342533948l,654459306l,3188396048l,3373015174l,1466479909l,544179635l,3110523913l,3462522015l,1591671054l,702138776l,2966460450l,3352799412l,1504918807l,783551873l
	                ,3082640443l,3233442989l,3988292384l,2596254646l,62317068l,1957810842l,3939845945l,2647816111l,81470997l,1943803523l,3814918930l,2489596804l,225274430l,2053790376l,3826175755l,2466906013l,167816743l,2097651377l
	                ,4027552580l,2265490386l,503444072l,1762050814l,4150417245l,2154129355l,426522225l,1852507879l,4275313526l,2312317920l,282753626l,1742555852l,4189708143l,2394877945l,397917763l,1622183637l,3604390888l,2714866558l
	                ,953729732l,1340076626l,3518719985l,2797360999l,1068828381l,1219638859l,3624741850l,2936675148l,906185462l,1090812512l,3747672003l,2825379669l,829329135l,1181335161l,3412177804l,3160834842l,628085408l,1382605366l
	                ,3423369109l,3138078467l,570562233l,1426400815l,3317316542l,2998733608l,733239954l,1555261956l,3268935591l,3050360625l,752459403l,1541320221l,2607071920l,3965973030l,1969922972l,40735498l,2617837225l,3943577151l
	                ,1913087877l,83908371l,2512341634l,3803740692l,2075208622l,213261112l,2463272603l,3855990285l,2094854071l,198958881l,2262029012l,4057260610l,1759359992l,534414190l,2176718541l,4139329115l,1873836001l,414664567l
	                ,2282248934l,4279200368l,1711684554l,285281116l,2405801727l,4167216745l,1634467795l,376229701l,2685067896l,3608007406l,1308918612l,956543938l,2808555105l,3495958263l,1231636301l,1047427035l,2932959818l,3654703836l
	                ,1088359270l,936918000l,2847714899l,3736837829l,1202900863l,817233897l,3183342108l,3401237130l,1404277552l,615818150l,3134207493l,3453421203l,1423857449l,601450431l,3009837614l,3294710456l,1567103746l,711928724l
	                ,3020668471l,3272380065l,1510334235l,755167117l};
	        byte[] seedEncode={65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,97,98,99,100,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,48,49,50,51,52,53,54,55,56,57,45,95};

	        int b=0;
	        long c=4294967295l;

	        int d=0;
	        for (; d<srcStr.length(); d++) {
	            int e=srcStr.charAt(d);
	            b=(255&b)<<8^seed16[(int)(255&((65280 & b)>>8^e))];
	            c=seed32[(int)(255&(c^e))]^c>>>8;
	        }
	        b=b&65535;
	        c=~c;
	        byte[] a={seedEncode[b>>10&63],seedEncode[b>>4&63],seedEncode[(int)(b<<2&60|c>>30&3)],seedEncode[(int)(c>>24&63)],seedEncode[(int)(c>>18&63)],seedEncode[(int)(c>>12&63)],seedEncode[(int)(c>>6&63)],seedEncode[(int)(c&63)]};
	        byte[] ret=new byte[8];
	        for (d=0; d<a.length; d++) {
	            ret[d]=a[d];
	        }
	        return new String(ret);
	    }
}