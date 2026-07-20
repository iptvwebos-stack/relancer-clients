package com.example.data

import kotlinx.coroutines.flow.Flow

class ClientSubscriptionRepository(private val dao: ClientSubscriptionDao) {
    val allSubscriptions: Flow<List<ClientSubscription>> = dao.getAllSubscriptions()

    suspend fun insert(subscription: ClientSubscription) {
        dao.insertSubscription(subscription)
    }

    suspend fun insertAll(subscriptions: List<ClientSubscription>) {
        dao.insertSubscriptions(subscriptions)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }

    suspend fun update(subscription: ClientSubscription) {
        dao.updateSubscription(subscription)
    }

    suspend fun delete(subscription: ClientSubscription) {
        dao.deleteSubscription(subscription)
    }

    suspend fun getCount(): Int {
        return dao.getCount()
    }

    suspend fun checkAndPrepopulate() {
        if (dao.getCount() == 0) {
            val subscriptions = parseInitialCsv()
            dao.insertSubscriptions(subscriptions)
        }
    }

    private fun parseInitialCsv(): List<ClientSubscription> {
        val csvData = """65q7fabdellatif2,k4U3hBmTbH7MBWZ,3/19/27 14:46,317 days,Activated,260
omarbobsaheb65u8075mbarek,qefgBwXer9JXkbL,3/19/27 12:06,317 days,Activated,260
65u8075mbarek,0z3sh2U1oXEeIyu,3/19/27 11:38,317 days,Activated,259
tvboxanas,wW5em0JoNiBOGkK,3/15/27 18:17,313 days,Activated,256
65q7fabdellatif,tZ2TDtsITJD7uhp,3/15/27 14:50,313 days,Activated,256
50du8075haymohamadi,XxBGM9uNMSm5pCr,3/14/27 18:06,312 days,Activated,255
karim28:07:08:d7:cb:f2,eC4KT4cYyX4poXl,3/4/27 18:03,302 days,Activated,245
hajjaDahbi,4GbQAdR4zGhvjmj,2/28/27 21:37,298 days,Activated,241
aminecafeivazionroomiptv2,XL2NEhhyKLdMlou,2/24/27 13:38,294 days,Activated,237
sposb1:00:03:fc:f4:52,3tHCHrMUGv8UmmS,2/23/27 21:21,293 days,Activated,236
58u8075zaidmaghfour,0WaYDkOAmRdlbJX,2/22/27 14:49,292 days,Activated,235
sahebjabbor,E3T9k0RawHKEZ96,2/19/27 13:05,289 days,Activated,232
lgd8:fb:45:06:24:de,aQqF4zq4Grsp6Ov,2/17/27 18:15,287 days,Activated,230
jawadIPTVfrère,BlKBN21c6CXQ8Af,2/15/27 21:14,285 days,Activated,228
43xiaomiabderrahim,4kj0g373qu0zJq1,2/15/27 14:10,285 days,Activated,228
reda-taibighazi,tfzMJ9Th8XCgqNp,2/14/27 22:34,284 days,Activated,227
mohaespagne,MTBXNqz8oJsOTFn,2/14/27 21:56,284 days,Activated,227
webosbrahim,Nuy7YNOe7GGDKhu,2/14/27 18:54,284 days,Activated,227
43vectronbrahim,OwypZHB7DAkS6Pv,2/14/27 12:33,284 days,Activated,227
lahcenSiera32vida,WXt0Po3q2dRlEIJ,2/11/27 14:08,281 days,Activated,224
abdelhadigholam,vB1LzB0WQKyW6vW,2/7/27 20:39,277 days,Activated,220
hajarmgs,VhVRBrvrWY9Lp60,2/7/27 13:52,277 days,Activated,220
sharpsamsung01,45rukJ7Au0MNO5A,2/4/27 17:08,274 days,Activated,217
abdelhadimr:nn:56:ru:z5:fy,hIU7um4psOQGrqv,2/3/27 11:19,273 days,Activated,215
youssef28:af:42:5b:2e:c7,jxMpiY3t5iCkVNW,2/2/27 19:22,272 days,Activated,215
Abdellah2Mzahra,XynBsEq2LqWFn9z,2/1/27 15:30,271 days,Activated,214
harounteddy,FjE0MbfwAUtGsOD,1/31/27 17:46,270 days,Activated,213
Abdellah2Mbilal,p23Dr65mGH4PAeh,1/26/27 16:59,265 days,Activated,208
abdelhadihaier,ZnW2XvJqqxwL88w,1/18/27 13:59,257 days,Activated,200
abdellahomaraibo,gYUdKfMqAcRFfI1,1/18/27 10:22,257 days,Activated,199
Zouhair70:b1:3d:0b:55:41,wTvtmPdF0SZcw31,1/17/27 21:23,256 days,Activated,199
harounahmed,ZVETFcpdPtzTNeb,1/17/27 14:54,256 days,Activated,199
50xiaomiabdelali,y7aRAWtdw22Lqt4,1/17/27 11:09,256 days,Activated,198
lgrochenoire4,p5AWRuf9NKaOEoq,1/16/27 15:00,255 days,Activated,198
abdelhaditcl2,EzFkWOZVR6sF3qx,1/13/27 21:53,252 days,Activated,195
france2bob5c:c1:d7:3b:34:05,J7KUy6gA30BpWgA,1/13/27 21:23,252 days,Activated,195
75u8075ahmedtissir,kwXvPZ9UjzCpFN4,1/8/27 19:45,247 days,Activated,190
50xiaomimhd4,PthLvxcLSv2f7tu,1/7/27 17:39,246 days,Activated,189
50du8075hajja,aGLbxIGES7qrcDS,1/6/27 9:32,245 days,Activated,187
776060508,hfrJ3HiKox55I8Z,1/4/27 16:18,243 days,Activated,186
redahicham04,by2KOsCDuHciKcf,1/4/27 12:59,243 days,Activated,186
Abdellah2M,2NzrEW7Hvfe4pMB,1/3/27 13:48,242 days,Activated,185
harounbc:45:5b:bd:e6:dd,ZYCoRuaBDR9En06,1/3/27 11:35,242 days,Activated,184
yassinecnss,zOPk2IaOBZGSQ3z,1/3/27 9:36,242 days,Activated,184
nokriredouane001,EwfatXx5EV6ONvE,12/29/26 17:38,237 days,Activated,180
ElauafiAbdellahd4:9d:c0:1f:93:0e,1btNSmnWPIE5MuW,12/29/26 13:37,237 days,Activated,180
fitcohananvend,N2Ozve1tF3TPQuL,12/29/26 12:47,237 days,Activated,180
lgrochenoir3,0croSDzv4d12NAy,12/27/26 19:43,235 days,Activated,178
50ut73bob,zsIozU0xDeGEv5k,12/27/26 12:09,235 days,Activated,178
32t5300-68:72:c3:40:ed:b7,CFbvpz1jqLlCmvV,12/26/26 15:57,234 days,Activated,177
fadwadazzabob,hlkEjBMuQOqYsmj,12/25/26 15:13,233 days,Activated,176
jamalgard,B6yxxBZb7lnXy4i,12/22/26 15:58,230 days,Activated,173
55p8khamada,sZHX37YisoM2HxO,12/21/26 23:16,229 days,Activated,172
samir65q70,0Iavne8MpxXOCiS,12/21/26 13:44,229 days,Activated,172
khalidborj55xiaomi,TPCcizGxZsM0Xcg,12/20/26 19:56,228 days,Activated,171
Slimanenajibob,hCdPdSv19POLlD4,12/20/26 15:15,228 days,Activated,171
mhmelinanividaa,Hop7Sv8LXEjFWiQ,12/19/26 21:53,227 days,Activated,170
55cu8chelalatpere,NM6uFv7IUAdr36a,12/19/26 19:30,227 days,Activated,170
zenbixtream,q1g0JWNJjYa4Cwn,12/16/26 19:08,224 days,Activated,167
43du8075lmanjra,mFEzOuzGy1PbNRf,12/15/26 13:31,223 days,Activated,166
arbahayoub,6wtbfTrSVfGE0tp,12/14/26 15:18,222 days,Activated,165
arbahahmed,kbTJXtxX0794taz,12/14/26 14:27,222 days,Activated,165
abdelhaditcl,U2XWVPKojx4tQVG,12/13/26 17:31,221 days,Activated,164
tvboxkobrax1,i5Q8qNwH1kAWSh7,12/13/26 15:21,221 days,Activated,164
abdelatifsabir,7F14PgmPKxzk1LD,12/12/26 20:02,220 days,Activated,163
spos001,9ZvvtJ9V80gPijn,12/8/26 12:23,216 days,Activated,159
Samsungiptvcafeivazion,nZqkE9ivlZA15UL,12/7/26 12:48,215 days,Activated,158
43xiaomiNajia,pzAvbA4H6KwdA73,12/7/26 10:56,215 days,Activated,157
abdelhadi4c:57:39:e2:e2:d0,PlMF4o8FcuV51e7,12/5/26 13:47,213 days,Activated,156
xiaomixciptv,8NnOQwA74a2RtEt,12/4/26 19:39,212 days,Activated,155
abdelhadilr:ti:ys:xo:33:dd,3nnj9cmuhd4Aqlb,12/2/26 19:28,210 days,Activated,153
abdelhadi9b:fc:48:40:25:c3,YyTz85GVyJntf6f,11/29/26 18:38,207 days,Activated,150
rabiamediouna002,Gz3d8Wv4wj5HoBO,11/29/26 12:46,207 days,Activated,150
lgiptvrochenoir2,5JDq7GJn8UphP1z,11/29/26 12:26,207 days,Activated,150
mhdelinaniluxsat,BAEpRcWjArGBMhK,11/26/26 18:14,204 days,Activated,147
abdelhadi004ca:xc:sl:wr:jz:ys,vZcnOB4gLuKrQyi,11/25/26 16:16,203 days,Activated,146
abdelhadi003c2:wg:qt,lmHIWBcVAPrTYvc,11/23/26 21:07,201 days,Activated,144
55morsatlakbir,s57f2GhMiKhsw7c,11/23/26 12:03,201 days,Activated,144
iptvfranceharounphone,0TVeLLjJjD1WxQ4,11/22/26 19:04,200 days,Activated,143
khalid55cu7175lg,FBQYtmJNMY1ju31,11/19/26 16:54,197 days,Activated,140
55xiaomiabdo,M0mNYb3OsGvVgl8,11/19/26 16:44,197 days,Activated,140
France2box2,9driT4UnOWkdPYS,11/15/26 15:51,193 days,Activated,136
aminecafeivazionroomiptv,OmSAjTAN0jgDBBj,11/15/26 12:50,193 days,Activated,136
Samsungroomiptv,Zp9clqXuUUECZGy,11/13/26 0:58,190 days,Activated,133
43xiaomiabdelmajid,FdCgZ6CLsYOLnhm,11/8/26 10:45,186 days,Activated,128
abdelhadiHisense75,GiNkhC53yyX7YlK,11/7/26 18:00,185 days,Activated,128
43du7175hajja,88FvIfUs86nL2aZ,11/2/26 17:40,180 days,Activated,123
55du7175aidi,dy0HXAjFTnRXgIB,11/2/26 9:45,180 days,Activated,122
aminebob,9rUcOpsSHucZf6A,11/5/26 19:23,183 days,Activated,126
rabiamediouna001,7tZfn0qgN1b9nnm,10/29/26 10:44,176 days,Activated,118
iptvderouabox,0svCCnu5kSBCVui,10/27/26 20:13,174 days,Activated,117
abdelhadi001,MR8RtmPkULsl3yu,10/26/26 14:45,173 days,Activated,116
hatim2,LbFiuBtRPuMwVVP,10/16/26 18:25,163 days,Activated,106
saidberrchidlg,7Uabre9zjYsswt4,10/15/26 17:01,162 days,Activated,105
atyqsamira,cn431USFC0DLxYx,10/10/26 18:38,157 days,Activated,100
jawadiptvxiaomi55,tMhum2YsutMRA83,10/4/26 13:46,151 days,Activated,94
lgrochenoir,pA0DqQJplwCCWVR,10/2/26 18:31,149 days,Activated,92
Abdessamadcnss,zN8xPUUswL4jVM7,9/29/26 7:47,145 days,Activated,88
32xiaomielbari,doVcTWRkujuBMg8,9/25/26 12:07,142 days,Activated,85
55du7175hicham,diJ5sqNk9hgGEcj,9/15/26 12:34,132 days,Activated,75
Saidkods32sams,RdKpvP3MPQSyl5W,9/11/26 20:42,128 days,Activated,71
55u8075fuDiaa2,u7sok23g,9/11/26 19:50,128 days,Activated,71
55u8075fuDiaa,whffjX0WbzrsdMF,9/9/26 10:59,126 days,Activated,68
55du8075salah,KUOrYrtLwZcflWC,9/4/26 10:33,121 days,Activated,63
50du8075ghizlane,nrw3bsq5gkTNY3j,9/2/26 15:22,119 days,Activated,62
65du7175tamir,EF6sz72I4x56wna,8/25/26 18:01,111 days,Activated,54
iptvfranceharounbob,7TaiRmumurVsDIK,8/23/26 20:13,109 days,Activated,52
Youssef43lgKNG,xhR0vQ3USQAekza,8/9/26 21:51,95 days,Activated,38
Franceharounvoisin,5Grau7qh3esE9Rp,8/7/26 21:22,93 days,Activated,36
Franceharounfrere,kg6tw14U9LUnKr3,8/7/26 16:10,93 days,Activated,36
43du7175taxiRg,cKeIn3f5SP05aO0,8/2/26 13:16,88 days,Activated,31
32t5300bob,XXbWKn5l7MTay2r,7/24/26 14:29,79 days,Activated,22
redaLotfiEspagne,zfTkuycCslh5BXk,7/18/26 21:13,73 days,Activated,16
boxsenicmhd,1yyK7s8YBA2LzPQ,7/13/26 14:28,68 days,Activated,11
tvboxx96plus,CkVserFce4qGsPF,7/8/26 15:30,63 days,Activated,6
32xiaomiBornazil,AEq4w8SAsOMsgBJ,7/5/26 17:01,60 days,Activated,3
Redouaneiptv,a7mWWoKePFdUYbI,6/29/26 21:02,54 days,Activated,-3
43d5300adil,EOJcd8sUZz7Mxp0,6/29/26 18:12,54 days,Activated,-3
Youssef43lgbob,vM8DJ8nOAQkvHhv,6/29/26 11:35,54 days,Activated,-4
43xiaomi03,0WMKb3bFvteRhoJ,6/26/26 13:37,51 days,Activated,-6
32xiaomi03,M9g7mVzsRZ6S87T,6/25/26 10:00,50 days,Activated,-8
yahya01,Mfgq2fNKtKKbUn7,6/19/26 13:11,44 days,Activated,-13
Xiaomi435,xsXLZGzxiHPuzFi,6/18/26 12:21,43 days,Activated,-14
zenibi55du7,ImdakOI4bEVr6i3,6/16/26 14:47,41 days,Activated,-16
H96MaxKNG,F4n2bKntF9mkkdJ,6/9/26 13:20,34 days,Activated,-23
Mostafapneux,nhFip7BgD6Ju2vt,5/27/26 15:14,21 days,Activated,-36
43du7175bob,nkesj8GXGuM9tw5,5/24/26 9:26,18 days,Activated,-40
jabri01,qEbmhGY1YgF2gZz,5/22/26 15:50,16 days,Activated,-41
mostafa50du8075,kZ0WX0WpXQolEiv,5/17/26 18:18,11 days,Activated,-46
ktnplayer,cvTkUvT99nOHTeO,5/15/26 17:45,9 days,Activated,-48
32xiaomi,17nLv2V9Z7zQZTh,5/11/26 18:04,5 days,Activated,-52
chaikh2,6z2ZnpyNXm6PAkF,5/11/26 13:18,5 days,Activated,-52
samiralaaroussi,ZpAWY1r5TyzLccN,5/10/26 19:56,4 days,Activated,-53
france2mibox,AnGf4gCuAECdF3z,5/10/26 10:41,4 days,Activated,-54
anass2iptv,3ZACzo9LzmqrjLU,5/8/26 13:47,2 days,Activated,-55
43qxiaomi,YtXjjLj5piJgnSH,5/7/26 16:12,1 days,Activated,-56
mehdibobvir,63vbVTRdawuViUR,Expired,0 days,Expired,#VALUE!
reda2ibo,iwfb93UTeODNcpv,Expired,0 days,Expired,#VALUE!
55lg80006Iboplayer5,IUmZUBysDLFihgf,Expired,0 days,Expired,#VALUE!
80:8a:bd:15:b8:ae,oLNExZ0uJZWPHod,Expired,0 days,Expired,#VALUE!
43au7157bob,4ZskRVwKpVxoAgx,Expired,0 days,Expired,#VALUE!
55lg80006Iboplayer4,JKzcnZrltuNDora,Expired,0 days,Expired,#VALUE!
55lg80006Iboplayer3,oBcEGHhkOiTqjpt,3/23/27 14:54,321 days,Activated,264
samsbob2,IOVrojKbuBbqSFR,Expired,0 days,Expired,#VALUE!
samsbob,MeuORMUJAyD5ez8,3/18/27 22:05,316 days,Activated,259
france2,WEbTWEwCi3LD6c2,3/16/27 18:54,314 days,Activated,257
65du8075bob2,XQdMFL3qCwTD8hN,Expired,0 days,Expired,#VALUE!
55xiaomi55,gRzAVuffcUC8PU0,Expired,0 days,Expired,#VALUE!
tohamia2,FWKpudvynSIRdVB,Expired,0 days,Expired,#VALUE!
50du7175bobbornazil,eKTzh5sqbhBlgOX,2/22/27 12:59,292 days,Activated,235
50xiaomi2,vVIyMNuFB0r4sWk,Expired,0 days,Expired,#VALUE!
43du7000bob,WIqjWCy61luEvM7,Expired,0 days,Expired,#VALUE!
32xiaomi1,gbgiPte7vrvOqxf,Expired,0 days,Expired,#VALUE!
43t5300bob,RK686Cdy6H9VVuJ,Expired,0 days,Expired,#VALUE!
43vectron,kndC5aUqRDrHB34,2/25/27 14:10,295 days,Activated,238
Mhd32xiaomi,XVagzGNvonEYOOb,Expired,0 days,Expired,#VALUE!
karim50du7175bob,t4nL0oKulcm5B8h,2/16/27 13:08,286 days,Activated,229
iptvfirma2,fvFcZd0weZEF6Nf,3/14/27 16:38,312 days,Activated,255
50du8075bobf,ndOMES5nhIkVN1n,2/11/27 19:59,281 days,Activated,224
55lg80006/3,yrg4XCT5hrKHSeQ,Expired,0 days,Expired,#VALUE!
hatim,8OA3faK6RqqSL3R,Expired,0 days,Expired,#VALUE!
HichamOmara,AWdMs1xNGdHhfXg,2/3/27 10:14,273 days,Activated,215
55du7000bobnissan,Wbebo9Ij1c4uIlk,2/8/27 14:59,278 days,Activated,221
65xiaomi,D3akcAmVSjscqHQ,Expired,0 days,Expired,#VALUE!
jawadiptv65nq90,pLAJO4IHPeKA6Ra,2/2/27 10:50,272 days,Activated,214
55lg80006canada,XsKcAQRGjqpkzBv,1/25/27 21:19,264 days,Activated,207
policierbob,w38DsO5CLomR21A,1/21/27 17:15,260 days,Activated,203
40toyota2bob,QpI5mUaKkuZCvR2,Expired,0 days,Expired,#VALUE!
40t5300bob,3eTm1DBkY0yqiIW,1/15/27 15:25,254 days,Activated,197
minoual55mi,SxFGX9RzmueG5pR,2/18/27 20:01,288 days,Activated,231
younesbengrir,R5TjcbvS3jtmKZ2,1/9/27 16:24,248 days,Activated,191
miboxdroid,vm76Ka8Mhku617J,1/13/27 8:21,251 days,Activated,194
daiko55du8075,Dkh03K5pEDmiIEE,2/23/27 21:08,293 days,Activated,236
50du7000,wSpJyt8okgegNOw,1/2/27 21:29,241 days,Activated,184
43xiaomiking2,9MJmVD10mzUKtEU,Expired,0 days,Expired,#VALUE!
43xiaomiking,e2GHRIUnpIZLxcm,1/10/27 11:19,249 days,Activated,191
frèrelmkadem,zNikfZelTys5Er0,12/31/26 10:25,239 days,Activated,181
43visionsabri,KZuG2liLmuyu78H,Expired,0 days,Expired,#VALUE!
tvboxx96,q66rkiYA21QId9a,Expired,0 days,Expired,#VALUE!
55xiaomi99,kjjawlkviMZcJMk,12/21/26 13:02,229 days,Activated,172
itel43,wb85u1GKzhLRfM5,12/21/26 19:55,229 days,Activated,172
abonasrbob,SsfjH3Npn1jGIpL,12/15/26 17:40,223 days,Activated,166
43qled2,NQvQg9G2lI164tC,12/20/26 21:44,228 days,Activated,171
43visionelfadl,SPhO7yYy359cSkO,Expired,0 days,Expired,#VALUE!
43qled,3Bl4TS8tby12oAL,12/8/26 17:35,216 days,Activated,159
Xiaomi55and,99fLIOHB7HF54Fu,Expired,0 days,Expired,#VALUE!
lachguer,bLDlnHDKdaKZAA1,12/2/26 9:28,210 days,Activated,152
43du8085bob,k3i2sj0c,Expired,0 days,Expired,#VALUE!
65du8075bob,f2co0inw,Expired,0 days,Expired,#VALUE!
50du8075bob,242x82xf,Expired,0 days,Expired,#VALUE!
immobmy,bfaghgu7,Expired,0 days,Expired,#VALUE!
mhdamine,swgwg2ji,Expired,0 days,Expired,#VALUE!
yahya001,ifjlaqf6,10/18/26 15:12,165 days,Activated,108
ahmedjabiri,kytfiszs,Expired,0 days,Expired,#VALUE!
43du8075,YYl0tkMz8BpKYRu,10/16/26 21:08,163 days,Activated,106
55du8,2uluxrxq,Expired,0 days,Expired,#VALUE!
55xiaomiQled,xlkmvefo,10/6/26 13:25,153 days,Activated,96
iptvderoua,7otruvr1,10/9/26 19:50,156 days,Activated,99
mouradainharoda,lp1hlc1r,Expired,0 days,Expired,#VALUE!
50xiaomi,kaasu3wb,Expired,0 days,Expired,#VALUE!
43cu8075,njviob56,11/5/26 17:03,183 days,Activated,126
khalidborj2,7h8dz7mm,Expired,0 days,Expired,#VALUE!
filali2samsung,qzh6w7mq,Expired,0 days,Expired,#VALUE!
filalisamsung,c8rp06vc,Expired,0 days,Expired,#VALUE!
ea:56:5d:dc:ef:8b,1v5ejvwj,Expired,0 days,Expired,#VALUE!
fitco32,0q8tjszm,Expired,0 days,Expired,#VALUE!
55cu8chelalat,jt3c5w8c,7/30/26 21:56,85 days,Activated,28
youssefxiaomi43,f5tie5ra,Expired,0 days,Expired,#VALUE!
omarramiborj,t6qt4di9,Expired,0 days,Expired,#VALUE!
omaralborj,gyhe5y86,Expired,0 days,Expired,#VALUE!
43xiaomi,ine20lak,Expired,0 days,Expired,#VALUE!
khalid55cu7175,zy82emrf,6/19/26 18:13,44 days,Activated,-13
elandalousy,yk1vz1fc,Expired,0 days,Expired,#VALUE!
bader50bu8,6f8dckp8,6/8/26 16:07,33 days,Activated,-24
2tvxiaomi50,jmt5ev46,Expired,0 days,Expired,#VALUE!
vision55ibo,jtx3ze4x,6/5/26 12:43,30 days,Activated,-27
32t5300ibo,mdelv4jz,5/23/26 13:31,17 days,Activated,-40
videNL,suqakeg5,Expired,0 days,Expired,#VALUE!
radiosouira,mnshkhl6,Expired,0 days,Expired,#VALUE!
azzdine55cu8,42rgyrmd,Expired,0 days,Expired,#VALUE!
iptvharoun0,9Ju4jEZxaX9tcXQ,Expired,0 days,Expired,#VALUE!
43lgibop,1cuct6yh,Expired,0 days,Expired,#VALUE!
younes65,km8btt7v,Expired,0 days,Expired,#VALUE!
55lg80006,4slp8nae,Expired,0 days,Expired,#VALUE!
7hamzaibo,b1ilrduv,Expired,0 days,Expired,#VALUE!
55xiaomi,igdmpn6t,Expired,0 days,Expired,#VALUE!
atifilycelyouti,jqs81hry,Expired,0 days,Expired,#VALUE!
6hamzaibo,qy8r6jhc,Expired,0 days,Expired,#VALUE!
3ikamatouladzian,oa142vx4,Expired,0 days,Expired,#VALUE!
2ikamatSalam,eptkgjbo,Expired,0 days,Expired,#VALUE!
ikamatSalam,rwus9uv0,Expired,0 days,Expired,#VALUE!
abdelghafourIPTV,b8u20smh,Expired,0 days,Expired,#VALUE!
anassIPTV,7xusronw,5/7/26 20:24,1 days,Activated,-56
moniriptv,f2zpnbo3,Expired,0 days,Expired,#VALUE!
6c70cb1074e4,nqnt1inf,Expired,0 days,Expired,#VALUE!
darifibo,8djisb2y,Expired,0 days,Expired,#VALUE!
5hamzaibo,8p6szo49,Expired,0 days,Expired,#VALUE!
4hamzaibo,6pevedoc,Expired,0 days,Expired,#VALUE!
3hamzaibo,f322fs29,Expired,0 days,Expired,#VALUE!
tohamia,qifcn74h,Expired,0 days,Expired,#VALUE!
Redouanebajja,6jzrbema,Expired,0 days,Expired,#VALUE!
ralaouilwelfa,jzcehdct,Expired,0 days,Expired,#VALUE!
sahebdjayji,glk85p5m,Expired,0 days,Expired,#VALUE!
djayji2ibo,29mubmvd,Expired,0 days,Expired,#VALUE!
3charif,8fi6r8et,Expired,0 days,Expired,#VALUE!
charifVirCih,i8g7ed31,Expired,0 days,Expired,#VALUE!
2hamza,yb55lbiw,Expired,0 days,Expired,#VALUE!
hamza1,3m9tpzp4,Expired,0 days,Expired,#VALUE!
khalidborj,zqcik122,12/20/26 19:32,228 days,Activated,171
Jabbour,djt9ipcb,Expired,0 days,Expired,#VALUE!"""

        val lines = csvData.split("\n")
        val result = mutableListOf<ClientSubscription>()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            val parts = trimmed.split(",")
            if (parts.size >= 5) {
                val login = parts[0]
                val password = parts[1]
                val remainingTime = parts[2]
                val days = parts[3]
                val status = parts[4]
                val expirationDays = if (parts.size >= 6) parts[5] else ""
                
                result.add(
                    ClientSubscription(
                        login = login,
                        password = password,
                        remainingTimeRaw = remainingTime,
                        daysRaw = days,
                        status = status,
                        expirationDaysRaw = expirationDays
                    )
                )
            }
        }
        return result
    }
}
