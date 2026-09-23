package com.topcop1000.standalone;

public final class PortalProfile {
    public enum Type { M3U, STALKER, XTREAM }
    public final Type type;
    public final String name;
    public final String server;
    public final String user;
    public final String secret;

    public PortalProfile(Type type,String name,String server,String user,String secret){
        this.type=type; this.name=name; this.server=server; this.user=user; this.secret=secret;
    }

    public boolean isValid(){
        if(type==null||server==null||server.trim().isEmpty()) return false;
        String s=server.trim().toLowerCase(java.util.Locale.ROOT); if(!(s.startsWith("http://")||s.startsWith("https://"))) return false;
        if(type==Type.XTREAM) return user!=null&&!user.trim().isEmpty()&&secret!=null&&!secret.trim().isEmpty();
        if(type==Type.STALKER) return secret!=null&&secret.trim().matches("(?i)^[0-9a-f]{2}(:[0-9a-f]{2}){5}$");
        return true;
    }
}
