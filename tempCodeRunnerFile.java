else {
			int filenumber = Integer.parseInt(ae.getActionCommand());
			try {
				JFileChooser jfc = new JFileChooser();
				File newfile = new File(filenameList.get(fileNumber-1));
				jfc.setSelectedFile(newfile);

				int r = jfc.showSaveDialog(null);

				if (r == JFileChooser.APPROVE_OPTION) {
					File file = jfc.getSelectedFile();
					chosenFilename = file.getName();

					JSONObject obj = new JSONObject();
					obj.put("type", "DOWNLOAD");
					obj.put("filenumber", filenumber);

					dosWriter.writeBytes(obj.toJSONString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}