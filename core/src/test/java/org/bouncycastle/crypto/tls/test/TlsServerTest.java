package org.bouncycastle.crypto.tls.test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;

import org.bouncycastle.crypto.tls.TlsServerProtocol;
import org.bouncycastle.util.io.Streams;
import org.bouncycastle.util.io.TeeOutputStream;

/**
 * A simple test designed to conduct a TLS handshake with an external TLS client.
 * <p/>
 * Please refer to GnuTLSSetup.html or OpenSSLSetup.html (under 'docs'), and x509-*.pem files in
 * this package (under 'src/test/resources') for help configuring an external TLS client.
 */
public class TlsServerTest
{
    private static final SecureRandom secureRandom = new SecureRandom();

    public static void main(String[] args)
        throws Exception
    {
        InetAddress address = InetAddress.getLocalHost();
        int port = 5556;

        ServerSocket ss = new ServerSocket(port, 16, address);
        while (true)
        {
            Socket s = ss.accept();
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("Accepted " + s);
            ServerThread t = new ServerThread(s);
            t.start();
        }
    }

    static class ServerThread
        extends Thread
    {
        private final Socket s;

        ServerThread(Socket s)
        {
            this.s = s;
        }

        public void run()
        {
            try
            {
                MockTlsServer server = new MockTlsServer();
                TlsServerProtocol serverProtocol = new TlsServerProtocol(s.getInputStream(), s.getOutputStream(), secureRandom);
                serverProtocol.accept(server);
                OutputStream log = new TeeOutputStream(serverProtocol.getOutputStream(), System.out);
                Streams.pipeAll(serverProtocol.getInputStream(), log);
                serverProtocol.close();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            finally
            {
                try
                {
                    s.close();
                }
                catch (IOException e)
                {
                }
                finally
                {
                }
            }
        }
    }
}
