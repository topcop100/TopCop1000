package com.topcop1000.standalone;

import android.net.Uri;

public final class PortalUrlBuilder {
    private PortalUrlBuilder(){}

    public static String normalizeServer(String raw){
        if(raw==null) return "";
        String s=raw.trim();
        if(!(s.startsWith("http://")||s.startsWith("https://"))) return "";
        while(s.endsWith("/")) s=s.substring(0,s.length()-1);
        return s;
    }

    public static String xtreamPlaylist(PortalProfile p){
        if(p==null||p.type!=PortalProfile.Type.XTREAM||!p.isValid()) return "";
        String base=normalizeServer(p.server); if(base.isEmpty()) return "";
        return base+"/get.php?username="+Uri.encode(p.user)+"&password="+Uri.encode(p.secret)+"&type=m3u_plus&output=ts";
    }

    public static String stalkerPortal(PortalProfile p){
        if(p==null||p.type!=PortalProfile.Type.STALKER||!p.isValid()) return "";
        return normalizeServer(p.server);
    }
}
