package no.royalone.audiobroadcast.utils;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

public class PortSeeker {
    public static final int MAX_PORT_NUMBER = 49151;
    public static final int MIN_PORT_NUMBER = 1;

    private PortSeeker() {
    }

    public static Set getAvailablePorts() {
        return getAvailablePorts(1, MAX_PORT_NUMBER);
    }

    public static int getNextAvailable() {
        return getNextAvailable(1);
    }

    public static int getNextAvailable(int fromPort) {
        if (fromPort < 1 || fromPort > MAX_PORT_NUMBER) {
            throw new IllegalArgumentException("Invalid start port: " + fromPort);
        }
        for (int i = fromPort; i <= MAX_PORT_NUMBER; i++) {
            if (available(i)) {
                return i;
            }
        }
        throw new NoSuchElementException("Could not find an available port above " + fromPort);
    }

    public static boolean available(int port) {
        Throwable th;
        if (port < 1 || port > MAX_PORT_NUMBER) {
            throw new IllegalArgumentException("Invalid start port: " + port);
        }
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ServerSocket ss2 = new ServerSocket(port);
            try {
                ss2.setReuseAddress(true);
                DatagramSocket ds2 = new DatagramSocket(port);
                try {
                    ds2.setReuseAddress(true);
                    if (ds2 != null) {
                        ds2.close();
                    }
                    if (ss2 != null) {
                        try {
                            ss2.close();
                        } catch (IOException e) {
                        }
                    }
                    ds = ds2;
                    ss = ss2;
                    return true;
                } catch (IOException e2) {
                    ds = ds2;
                    ss = ss2;
                    if (ds != null) {
                        ds.close();
                    }
                    if (ss != null) {
                        try {
                            ss.close();
                        } catch (IOException e3) {
                        }
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    ds = ds2;
                    ss = ss2;
                    if (ds != null) {
                        ds.close();
                    }
                    if (ss != null) {
                        try {
                            ss.close();
                        } catch (IOException e4) {
                        }
                    }
                    throw th;
                }
            } catch (IOException e5) {
                ss = ss2;
                if (ds != null) {
                    ds.close();
                }
                if (ss != null) {
                    ss.close();
                }
                return false;
            } catch (Throwable th3) {
                th = th3;
                ss = ss2;
                if (ds != null) {
                    ds.close();
                }
                if (ss != null) {
                    ss.close();
                }
                throw th;
            }
        } catch (IOException e6) {
            if (ds != null) {
                ds.close();
            }
            if (ss != null) {
              try {
                ss.close();
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
            return false;
        } catch (Throwable th4) {
            th = th4;
            if (ds != null) {
                ds.close();
            }
            if (ss != null) {
              try {
                ss.close();
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
        }
        return true;
    }

    public static Set getAvailablePorts(int fromPort, int toPort) {
        Throwable th;
        if (fromPort < 1 || toPort > MAX_PORT_NUMBER || fromPort > toPort) {
            throw new IllegalArgumentException("Invalid port range: " + fromPort + " ~ " + toPort);
        }
        Set result = new TreeSet();
        for (int i = fromPort; i <= toPort; i++) {
            ServerSocket s = null;
            try {
                ServerSocket s2 = new ServerSocket(i);
                try {
                    result.add(new Integer(i));
                    if (s2 != null) {
                        try {
                            s2.close();
                            s = s2;
                        } catch (IOException e) {
                            s = s2;
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    s = s2;
                }
            } catch (IOException e4) {
                if (s != null) {
                  try {
                    s.close();
                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                }
            } catch (Throwable th3) {
                th = th3;
            }
        }
        return result;
    }
}
