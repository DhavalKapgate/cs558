Name 				: 	Dhaval Kapgate, Amar Dharmendra Kumar
Email address   		:	dkapgat1@binghamton.edu, akumar14@binghamton.edu
Programming Language 		: 	JAVA
Platform			: 	Bingsuns
Reference			:	https://javadigest.wordpress.com/2012/08/26/rsa-encryption-example/ 
					http://www.c-sharpcorner.com/UploadFile/5fd9bd/digital-signature-with-plain-text-in-java/
					http://elmurod.net/en/index.php/archives/492
Execution steps:
					1.Use make to get all the class files (Bank.java Psystem.java Customer.java are compiled).
					2.The bank server is invoked as:    - java Bank <bank-port>
					3.The purchasing server is invoked as: - java Psystem <purchasing-port> <bank-domain> <bank-port>
					4.The client is invoked as: - java Customer <purchasing-domain> <purchasing-port>
					5.To clean/remove class files : make clean


code for encryption/decrption:
	1.In Customer.java
					public static String Hashing(String text)throws Exception
					   {
						  MessageDigest md = MessageDigest.getInstance("MD5");
						  Formatter form = new Formatter();
						  md.update(text.getBytes());
						  byte[] hash = md.digest();
						  for (byte bytes : hash)
							  form.format("%02x", bytes);
						  String hashed=form.toString();
						  return hashed;
					   }
					   public static byte[] encrypt(String Key, String to_encryp) 
						{
							byte[] encrypted = null;
							try 
							{
								ObjectInputStream OIS = new ObjectInputStream(new FileInputStream(Key));
								final PublicKey PubKey = (PublicKey) OIS.readObject();
								OIS.close();
								final Cipher cipher = Cipher.getInstance("RSA");
								cipher.init(Cipher.ENCRYPT_MODE, PubKey);
								encrypted = cipher.doFinal(to_encryp.getBytes());
							} 
							catch (Exception e) 
							{
							}
							return encrypted;
						}

						public static String decrypt(String Key, byte[] to_decrypt)
						{
							byte[] decrypted = null;
							try 
							{
								ObjectInputStream OIS = new ObjectInputStream(new FileInputStream(Key));
								final PrivateKey Privkey = (PrivateKey) OIS.readObject();
								OIS.close();
								final Cipher cipher = Cipher.getInstance("RSA");
								cipher.init(Cipher.DECRYPT_MODE, Privkey);
								decrypted = cipher.doFinal(to_decrypt);
							}	 
							catch (Exception e) 
							{
							}
							return new String(decrypted);
						}
						public static byte[] DSsign(String Key, byte[] toPsys)throws Exception
						{
							ObjectInputStream OIS = new ObjectInputStream(new FileInputStream(Key));
							final PrivateKey prkey = (PrivateKey) OIS.readObject();	
							OIS.close();
							Signature sign = Signature.getInstance("MD5WithRSA");
							sign.initSign(prkey);
							byte[] signatureBytes = sign.sign();
							return signatureBytes;
						}

	2.In Psystem.java
						public static byte[] encrypt(String key, String Cost) 
						{
							byte[] encrypted = null;
							try 
							{
								ObjectInputStream OIS = new ObjectInputStream(new FileInputStream(key));
								final PrivateKey key = (PrivateKey) OIS.readObject();
								OIS.close();
								
								final Cipher cipher = Cipher.getInstance("RSA");
								cipher.init(Cipher.ENCRYPT_MODE, key);
								encrypted = cipher.doFinal(Cost.getBytes());
							} 
							catch (Exception e) 
							{
							}
							return encrypted;
						}

						public static String decrypt(String key, byte[] fCust)
						{
							byte[] decrypted = null;
							try 
							{
								ObjectInputStream OIS = new ObjectInputStream(new FileInputStream(key));
								final PrivateKey key = (PrivateKey) OIS.readObject();	  
								OIS.close();
								
								final Cipher cipher = Cipher.getInstance("RSA");
									cipher.init(Cipher.DECRYPT_MODE, key);
								decrypted = cipher.doFinal(fCust);
							}	 
							catch (Exception ex) 
							{
							}
							return new String(decrypted);
						}
						public static boolean DSverify(String key, byte[] DS)throws Exception
						{
							ObjectInputStream OIS = new ObjectInputStream(new FileInputStream(key));
							final PublicKey publickey = (PublicKey) OIS.readObject();
							OIS.close();
							Signature sign = Signature.getInstance("MD5WithRSA");
							sign.initVerify(publickey);
							boolean verify = sign.verify(DS);
							return verify;
						}

	3. In Bank.java
					public static String decryptPrivate(String key_file, byte[] from_Psys)
					{
						byte[] decrypt = null;
						try 
						{
							ObjectInputStream OIS = new ObjectInputStream(new FileInputStream(key_file));
							final PrivateKey Privkey = (PrivateKey) OIS.readObject();	  
							OIS.close();
							final Cipher cipher = Cipher.getInstance("RSA");
							cipher.init(Cipher.DECRYPT_MODE, Privkey);
							decrypt = cipher.doFinal(from_Psys);
						}	 
						catch (Exception e) 
						{
						}
						return new String(decrypt);
					}
					public static String decryptPublic(String key_file, byte[] bal_deduction)
					{
						byte[] decrypt = null;
						try 
						{
							ObjectInputStream OIS = new ObjectInputStream(new FileInputStream(key_file));
							final PublicKey Pubkey = (PublicKey) OIS.readObject();	  
							OIS.close();
							final Cipher cipher = Cipher.getInstance("RSA");
							cipher.init(Cipher.DECRYPT_MODE, Pubkey);
							decrypt = cipher.doFinal(bal_deduction);
						}	 
						catch (Exception e) 
						{
						}
						return new String(decrypt);
					}


