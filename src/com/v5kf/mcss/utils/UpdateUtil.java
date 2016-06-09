package com.v5kf.mcss.utils;

public class UpdateUtil {

	/**
	 * 升级检测
	 * 
	 * @param locVersionName
	 * @param lastVersion
	 * @return 是否升级
	 */
	public static boolean checkUpdate(String locVersionName, String lastVersion) {
		boolean hasUpdate = false;
		String[] locVersionS = locVersionName.split("\\.");
		String[] lastVersionS = lastVersion.split("\\.");

		if (!locVersionName.equals(lastVersion)) {
			if (locVersionS != null && lastVersion != null) {
				int localLenth = locVersionS.length;
				int lastVerLenth = lastVersionS.length;

				// int netLenth = lastVersion.length();
				for (int i = 0; i < lastVerLenth; i++) {
					if (localLenth < lastVerLenth && i == localLenth) {
						hasUpdate = true;
						return hasUpdate;
					}

					if (Integer.valueOf(lastVersionS[i]) > Integer
							.valueOf(locVersionS[i])) {
						hasUpdate = true;
						return hasUpdate;
					} else if (Integer.valueOf(lastVersionS[i]) < Integer
							.valueOf(locVersionS[i])) {
						hasUpdate = false;
						return hasUpdate;
					}
				}
			}
		} else {
			hasUpdate = false;
		}
		return hasUpdate;
	}
}
