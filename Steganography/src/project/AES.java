package project;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class AES {

	public byte[][] state;
	byte[] key;
	public byte[][] w;
	byte[][] rCon;
	public byte[][] sBox;
	byte[][] iSBox;
	static GaloisField galField;

	int round; //Used to represent what round the encryption algorithms is on (will be 12 in the end)
	static final int blockSize = 4; //In Words (128 bits, 16 bytes)
	static final int keySize = 6; //In Words (192 bits, 24 bytes)
	static final int numberOfRounds = 12;
	
	/**
	 * This generates an instance of an AES encryption with a specified key.
	 * @param input This sets the key for the AES
	 */
	public AES(byte[] input){
		key = input;
		//Put input array of length 16 into 4x4 state array
		rCon = buildRCon();
		sBox = SBoxes.buildSBox();
		iSBox = SBoxes.buildISBox();
		w = keyExpansion(key);
		galField = new GaloisField();
	}
	/**
	 * This generates an instance of an AES encryption with an auto generated key.
	 */
	public AES(){
		key = generateKey();
		//Put input array of length 16 into 4x4 state array
		rCon = buildRCon();
		sBox = SBoxes.buildSBox();
		iSBox = SBoxes.buildISBox();
		w = keyExpansion(key);
		galField = new GaloisField();
	}
	
	/**
	 * Gets the key for an instance of an AES.
	 * @return Byte array that represents the key for the AES
	 */
	public byte[] getKey(){
		return this.key;
	}
	
	/**
	 * Used for testing purposes (timing and printing).
	 * @param args Not used
	 */
	public static void main(String[] args){
		byte[] testText = new byte[16];
		for (int i = 0; i < testText.length; i++)
			testText[i] = (byte) (Math.random() * 255);
		long startTime = System.currentTimeMillis();
		AES test = new AES();
		System.out.println("Time to make instance of AES: " + (System.currentTimeMillis() - startTime) + "ms");
		System.out.println("192 bit Key: " + arrayToString(test.key));
		startTime = System.currentTimeMillis();
		System.out.println("Input State:     " + arrayToString(testText));
		byte[] cipherText = null;
		try {
			cipherText = test.encrypt(testText);
		} catch (WrongSizeArrayException e) {
			e.printStackTrace();
		}
		/**
		System.out.println("rCon");
		TwoDimensionalArray.print(test.rCon);
		System.out.println("sBox");
		TwoDimensionalArray.print(test.sBox);
		System.out.println("iSBox");
		TwoDimensionalArray.print(test.iSBox);
		System.out.println("Key Expansion");
		TwoDimensionalArray.print(test.w);
		**/
		System.out.println("Encrypted State: " + arrayToString(cipherText));
		System.out.println("Decrypted State: " + arrayToString(test.decrypt(cipherText)));
		System.out.println("Time to Encrypt and then Decrypt: " + (System.currentTimeMillis() - startTime) + "ms");
	}
	
	/**
	 * Uses Java's built in libraries to generate a 192 bit AES key.
	 * @return Byte array that holds a 192 bit AES key
	 */
	public byte[] generateKey(){
		KeyGenerator kgen = null;
		try {
			//Use AES encryption
			kgen = KeyGenerator.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		//Use 192 bit AES encryption
		kgen.init(192);
		//Generate the Key then store it in aeskey
		SecretKey aeskey = kgen.generateKey();
		//Put that key in a byte[]
		byte [] encoded = aeskey.getEncoded();
		return encoded;
		/**
		String key = "6ea013aa065fd44347c2b9371bdb2df31c66770409c7ac40";
		byte[] output = new byte[24];
		for (int i = 0; i < 48; i = i + 2)
			output[i/2] = (byte) ((Character.digit(key.charAt(i), 16) << 4) + Character.digit(key.charAt(i+1), 16));
		return output;
		**/
	}

	/**
	 * Converts a byte array to a String with radix 16 (Hex).
	 * @param input Byte array to be turned into hex representation
	 * @return String with hexidecimal representation of the input
	 */
	public static String arrayToString(byte[] input){
		String output = "";
		for(byte b: input)
			output = output + Hexidecimal.byte2Hex(b);
		return output;
	}
	
	/**
	 * Takes your initial 6 row key and expands it into a 52 row key (round key) that is used for the encryption.
	 * 
	 * It expands the initial key by using subWord, rotWord, and XOR (exclusive or).
	 * Every 6th row gets rotWord and subWord applied. Every row gets XOR'd with the row 6 rows up.
	 * @param key the initial 6 row, 192 bit key
	 * @return the expanded 52 row round key used later on in encryption
	 */
	public byte[][] keyExpansion(byte[] key){
		byte[][] output = new byte[52][4];
		//Put key into the top of the output
		for (int row = 0; row < 6; row++){
			for (int col = 0; col < 4; col++){
				output[row][col] = key[(row * 4) + col];
			}
		}
		//Generate the rest using the existing key
		byte[] temp = new byte[4];
		//Loop set to run for 46 times (52-6)
		for (int row = keySize; row < blockSize * (numberOfRounds+1); ++row){
			//Set temp equal to the previously generate row
			for(int i = 0; i < 4; i++){
				temp[i] = output[row-1][i];
			}
			//Every 6th row, rotWord, subWord then XOR with a value from the rCon
			if (row % keySize == 0){
				temp = subWord(rotWord(temp));
				for(int i = 0; i < 4; i++){
					temp[i] = (byte)( (int)temp[i] ^ (int)rCon[row/keySize][i] );
				}
			}
			//Then take the row 6 rows up and XOR it with the row currently being generated
			for(int i = 0; i < 4; i++){
				output[row][i] = (byte) ((int)output[row-keySize][i] ^ (int)temp[i] );
			}
		}
		return output;
	}

	/**
	 * Encrypts the given byte array (of length 16).
	 * 
	 * Uses addRoundKey, subBytes, shiftRows, and mixColumns to encrypt the byte array (according to the AES standard).
	 * @param input the byte array to be encrypted
	 * @return the encrypted byte array
	 * @throws WrongSizeArrayException if the byte array length is not 16, this exception is thrown
	 */
	public byte[] encrypt(byte[] input) throws WrongSizeArrayException{
		if (input.length != 16)
			throw new WrongSizeArrayException();
		else{
			state = new byte[4][4];
			state = TwoDimensionalArray.fromSingleArray(input);
			addRoundKey(0);
			for (int round = 1; round < numberOfRounds; ++round){
				subBytes(); 
				shiftRows();  
				mixColumns(); 
				addRoundKey(round);
			}  
			subBytes();
			shiftRows();
			addRoundKey(numberOfRounds);
			return TwoDimensionalArray.toSingleArray(state);
		}
	}

	/**
	 * Generate the rCon 2-d array used in AES.
	 * @return the rCon 2-D byte array[11][4]
	 */
	public static byte[][] buildRCon(){
		byte[][] output = new byte[11][4];
		output[0][0] = (byte) 0x00;
		output[1][0] = (byte) 0x01;
		output[2][0] = (byte) 0x02;
		output[3][0] = (byte) 0x04;
		output[4][0] = (byte) 0x08;
		output[5][0] = (byte) 0x10;
		output[6][0] = (byte) 0x20;
		output[7][0] = (byte) 0x40;
		output[8][0] = (byte) 0x80;
		output[9][0] = (byte) 0x1b;
		output[10][0] = (byte) 0x36;
		for (int row = 0; row < 11; row = row + 1){
			for (int col = 1; col < 4; col = col + 1){
				output[row][col] = (byte) 0x00;
			}
		}
		return output;
	}

	/**
	 * Shifts the the indexes in the byte array one to the left.
	 * @param input the byte array to be rotated
	 * @return the rotated byte array
	 */
	public byte[] rotWord(byte[] input){
		byte tmp = input[0];
		input[0] = input[1];
		input[1] = input[2];
		input[2] = input[3];
		input[3] = tmp;
		return input;
	}

	/**
	 * Maps the values in the byte array to the sBox.
	 * @param input byte array to be substituted
	 * @return the substituted array
	 */
	public byte[] subWord(byte[] input){
		byte[] output = new byte[input.length];
		Integer hexInt;
		String hexString;
		char sRow;
		char sCol;
		for (int col = 0; col < input.length; col = col + 1){
			hexInt = (input[col] & 0xff);
			hexString = Integer.toHexString(hexInt);
			if (hexString.length() == 1){
				sRow = '0';
				sCol = hexString.charAt(0);
			}
			else{
				sRow = hexString.charAt(0);
				sCol = hexString.charAt(1);
			}
			try {
				output[col] = sBox[Hexidecimal.char2Hex(sRow)][Hexidecimal.char2Hex(sCol)];
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return output;
	}



	/**
	 * Take a value from the state table and XOR it with the inverse value from the w table.
	 * 
	 * If you get state[4][1] you would XOR that with w[1][4]. You flip the row and column for the w table.
	 * @param round the current round that the encryption algorithm is on
	 */
	public void addRoundKey(int round){
		for(int row = 0; row < 4; row++){
			for(int column = 0; column < 4; column++){
				state[row][column] = (byte) ((int)state[row][column] ^ (int)w[column + (round*4)][row]);
			}
		}
	}

	/**
	 * Maps every value in the state array to the sBox array.
	 * 
	 * Acts just like subWord but for the entire state array.
	 */
	public void subBytes(){
		Integer hexInt;
		String hexString;
		char sRow;
		char sCol;
		for (int row = 0; row < 4; row++){
			for (int col = 0; col < 4; col++){
				hexInt = (state[row][col] & 0xff);
				hexString = Integer.toHexString(hexInt);
				if (hexString.length() == 1){
					sRow = '0';
					sCol = hexString.charAt(0);
				}
				else{
					sRow = hexString.charAt(0);
					sCol = hexString.charAt(1);
				}
				try {
					state[row][col] = sBox[Hexidecimal.char2Hex(sRow)][Hexidecimal.char2Hex(sCol)];
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Rotates all of the values in the state array by the value of their row index
	 * 
	 * E.X. the first row (index 0) is rotated 0 spaces to the left. Row 4 (index 3) is rotated 3 to the left.s
	 */
	public void shiftRows(){
		byte[][] temp = new byte[4][4];
		for(int row = 0; row < 4; row++){
			for(int col = 0; col < 4; col++){
				temp[row][col] = state[row][col];
			}
		}
		for(int row = 1; row < 4; row++){
			for (int col = 0; col < 4; col++){
				state[row][col] = temp[row][(col + row) % blockSize];
			}
		}
	}

	/**
	 * mixes the Columns of the state array by using XOR and matrix multiplication 
	 */
	public void mixColumns()
	{
		byte[][] temp = new byte[4][4];
		for (int row = 0; row < 4; row++)  
		{
			for (int col = 0; col < 4; col++)
			{
				temp[row][col] = this.state[row][col];
			}
		}

		for (int col = 0; col < 4; col++)
		{
			this.state[0][col] = (byte) (galField.mulBy2((int)temp[0][col]) ^
					galField.mulBy3((int)temp[1][col]) ^
					((int)temp[2][col]) ^
					((int)temp[3][col]));

			this.state[1][col] = (byte) (((int)temp[0][col]) ^
					galField.mulBy2((int)temp[1][col]) ^
					galField.mulBy3((int)temp[2][col]) ^
					((int)temp[3][col]) );

			this.state[2][col] = (byte) (((int)temp[0][col]) ^
					((int)temp[1][col]) ^
					galField.mulBy2((int)temp[2][col]) ^
					galField.mulBy3((int)temp[3][col]) );

			this.state[3][col] = (byte) (galField.mulBy3((int)temp[0][col]) ^
					((int)temp[1][col]) ^
					((int)temp[2][col]) ^
					galField.mulBy2((int)temp[3][col]) );
		}
	}
	/*
	This is a great way to multiply two ints (base 16) together
	But in order to optimize the code we used array lookups instead
	public static int fieldMultiply(int a, int b) {
		int p = 0;
		for (int n=0; n<8; n++) {
			p = ((b & 0x01) > 0) ? p^a : p;
			boolean kyle = ((a & 0x80) > 0); //Is the first binary digit one?
			a = ((a<<1) & 0xFE);
			if (kyle)
				a = a ^ 0x1b;
			b = ((b>>1) & 0x7F);
		}
		return p;
	}
	*/
	
	/**
	 * 
	 */
	public byte[] decrypt(byte[] input){
		state = new byte[4][4];
		state = TwoDimensionalArray.fromSingleArray(input);
		addRoundKey(numberOfRounds);
		for (int round = numberOfRounds - 1; round > 0; --round){
			inverseShiftRows();  
			inverseSubBytes(); 
			addRoundKey(round);
			inverseMixCols(); 
		}  
		inverseShiftRows();
		inverseSubBytes();
		addRoundKey(0);
		return TwoDimensionalArray.toSingleArray(state);
	}

	public void inverseShiftRows(){

		byte[][] temp = new byte[4][4];
		for(int row = 0; row < 4; row++){
			for(int col = 0; col < 4; col++){
				temp[row][col] = state[row][col];
			}
		}
		for(int row = 1; row < 4; row++){
			for (int col = 0; col < 4; col++){
				state[row][col] = temp[row][((col + 4) - row) % blockSize];
			}
		}
	}

	public void inverseSubBytes(){

		Integer hexInt;
		String hexString;
		char sRow;
		char sCol;
		for (int row = 0; row < 4; row++){
			for (int col = 0; col < 4; col++){
				hexInt = (state[row][col] & 0xff);
				hexString = Integer.toHexString(hexInt);
				if (hexString.length() == 1){
					sRow = '0';
					sCol = hexString.charAt(0);
				}
				else{
					sRow = hexString.charAt(0);
					sCol = hexString.charAt(1);
				}
				try {
					state[row][col] = iSBox[Hexidecimal.char2Hex(sRow)][Hexidecimal.char2Hex(sCol)];
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void inverseMixCols(){

		byte[][] temp = new byte[4][4];
		for (int row = 0; row < 4; row++)  
		{
			for (int col = 0; col < 4; col++)
			{
				temp[row][col] = this.state[row][col];
			}
		}

		for (int col = 0; col < 4; col++)
		{
			this.state[0][col] = (byte) (galField.mulBy14((int)temp[0][col]) ^
					galField.mulBy11((int)temp[1][col]) ^
					galField.mulBy13((int)temp[2][col]) ^
					galField.mulBy9((int)temp[3][col]));

			this.state[1][col] = (byte) (galField.mulBy9((int)temp[0][col]) ^
					galField.mulBy14((int)temp[1][col]) ^
					galField.mulBy11((int)temp[2][col]) ^
					galField.mulBy13((int)temp[3][col]) );

			this.state[2][col] = (byte) (galField.mulBy13((int)temp[0][col]) ^
					galField.mulBy9((int)temp[1][col]) ^
					galField.mulBy14((int)temp[2][col]) ^
					galField.mulBy11((int)temp[3][col]) );

			this.state[3][col] = (byte) (galField.mulBy11((int)temp[0][col]) ^
					galField.mulBy13((int)temp[1][col]) ^
					galField.mulBy9((int)temp[2][col]) ^
					galField.mulBy14((int)temp[3][col]) );
		}
	}
}
