package de.hhu.tbus;

public class TbusSumoInformationServer extends Thread {
	private static TbusSumoInformationServer instance = null;
	private boolean running = false;
	private final Queue<String> queue;

	private TbusSumoInformationServer() {
		this.start();
	}

	public static TbusSumoInformationServer getInstance() {
		if (instance == null) {
			instance = new TbusSumoInformationServer();
		}

		return instance;
	}

	@Override
	public void run() {
		ServerSocket socket = null;
		Socket connection = null;

		try {
			socket = new ServerSocket(3330, 10);
			connection = socket.accept();

			while (running) {
				synchronized (queue) {
					while (running && queue.isEmpty()) {
						queue.wait();
					}
				}
				
				String entry;
				if (!queue.isEmpty()) {
					entry = queue.remove();
				}
				// TODO: Send entry over socket
			}
		} catch (IOException e) {
		} finally {
			if (socket != null) {
				socket.close();
			}
		}
	}

	public void setRunning(boolean r) {
		running = r;
	}

	public void addEntry(String entry) {
		synchronized (queue) {
			queue.add(entry);
			queue.notify();
		}
	}
}

