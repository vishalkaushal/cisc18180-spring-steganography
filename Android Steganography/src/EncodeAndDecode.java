import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * @author Stephen Herbein, Ronald Lewis, Kyle Tucker
 * Encodes text messages and images into an image and decodes them.
 */

public class EncodeAndDecode{

	/**
	 * Encodes a text message in an image.
	 * 
	 * Writes encoded image to a png file.
	 * 
	 * @param original Pathway of the file containing the image where the text will be hidden.
	 * @param writeTo Pathway of the file where the new image should be written to.
	 * @param message Text to be hidden in the image.
	 * @param anAES Object used for Encryption.
	 */
	
public static Bitmap encodeText(String original,String writeTo,String message,AES anAES){
		
		/**
		 * Holds the image to be encoded into.
		 */
		Bitmap encodedImage = BitmapFactory.decodeFile(original);
		/* Gets a Bitmap from the given File. */
		/**
		 * Indicates how much the encoded image must be scaled up.
		 */
		int scaleFactor = 1;
		while((message.length() + 2) > (scaleFactor * scaleFactor * encodedImage.getHeight() * encodedImage.getWidth())){
			scaleFactor += 1;
		}
		/* Determines the scaleFactor needed to fit all the
		   information from the message into the image.
		   i.e. the image must have enough pixels to fit the
		   message, the start code, and the end code. */
		encodedImage = scaleUp(encodedImage,scaleFactor);
		/**
		 * Width of the encoded image.
		 */
		int width = encodedImage.getWidth();
		/**
		 * Height of the encoded image.
		 */
		int height = encodedImage.getHeight();
		/* Variables used to move through the encoded image. */
		/**
		 * Integers representing the message.
		 */
		int[] messageData = new int[message.length()];
		for (int i = 0; i < messageData.length; i += 1){
			messageData[i] = (int) message.charAt(i);
		}
		/* Converts each message character to an integer. */
		messageData = encryption(messageData,anAES);
		/* Encrypts the message. */
		for (int j = 0; j < width; j+= 1){
			/* Controls movement through the image 
			   horizontally. */
			for (int i = 0; ((((j * height) + i) <= 
					messageData.length + 1) && (i < height)); 
					i += 1){
				/* Controls movement through the image 
				   vertically. */
				if (i == 0 && j == 0){
					encodedImage.setPixel(j,i,changeColor(
							(encodedImage.getPixel(
									(j),(i))),342));
					/* Places 342 in the first pixel of the
					   image to indicate a text message is
					   within it. */
				}
				else if (((j * height) + i) == (messageData.length + 1)){
					encodedImage.setPixel(j,i,changeColor(
							(encodedImage.getPixel((j),(i))),423));
					/* Places 423 in the current pixel of the
					   image to indicate the end of the encrypted message
					   has been reached. + 1 accounts for start 
					   up code. */
					try{
						FileOutputStream output = new FileOutputStream(writeTo);
						encodedImage.compress(Bitmap.CompressFormat.PNG,0,output);
					}
					catch (Exception e){
						System.out.println(e);
					}
					/* Writes encoded image to a file and displays it. */
					return encodedImage;
				}
				else{
					encodedImage.setPixel(j,i,changeColor(encodedImage.getPixel(
									j,i),messageData[(j * height) + i - 1]));
					/* Changes the Color of the current pixel
				   	so that a character of the message is
				   	held within it. */
				}
			}
		}
		return encodedImage;
		
	}

	/**
	 * Encodes an image in another image.
	 * 
	 * Writes encoded image to a png file.
	 * 
	 * @param hideThis Pathway of the file containing image to be hidden.
	 * @param hideIn Pathway of the file containing image that will contain other image.
	 * @param writeTo Pathway of the file where the new image should be written to.
	 * @param anAES Object used for Encryption.
	 */

	public static Bitmap encodePicture(String hideThis,String hideIn,String writeTo,AES anAES){
	
		/**
		 * Holds image to be hidden in another image.
		 */
		Bitmap imageToHide = BitmapFactory.decodeFile(hideThis);;
		/**
		 * Holds image where the other image will be hidden.
		 */
		Bitmap placeToHide = BitmapFactory.decodeFile(hideIn);
		/**
		 * Indicates how much the encoded image must be scaled up.
		 */
		int scaleFactor = 1;
		while(((3 * imageToHide.getWidth() * imageToHide.getHeight()) + 2 > ((scaleFactor * scaleFactor * placeToHide.getHeight() * placeToHide.getWidth())))){
			scaleFactor += 1;
		}
		/* Determines the scaleFactor needed to fit all the
	   	information from the original image into the new image.
	   	i.e. the new image must have 3 times as many
	   	pixels as the original + 3 after scaling to fit the image and 
	   	its dimensions.*/
		placeToHide = scaleUp(placeToHide,scaleFactor);
		/**
		 * Width of the image to be hidden.
		 */
		int imageWidth = imageToHide.getWidth();
		/**
		 * Height of the image to be hidden.
		 */
		int imageHeight = imageToHide.getHeight();
		/* Variables used to move through the image to be hidden. */
		/**
		 * Numbers representing the pixels of the image to be hidden.
		 */
		int[] imagePixels = new int[3 * imageWidth * imageHeight];
		/**
		 * Numbers representing the current pixel of the image to be hidden.
		 */
		int[] currentPixelData = new int[3];
		for (int j = 0; j < imageWidth; j+= 1){
			/* Controls movement through the image to be hidden horizontally. */
			for (int i = 0; i < imageHeight; i += 1){
				/* Controls movement through the image to be hidden vertically. */
				currentPixelData = getPixelData(imageToHide.getPixel(j,i));
				/* Gets the integers representing the current pixel in the original image. */
				imagePixels[3 * ((j * imageHeight) + i)] = currentPixelData[0];
				imagePixels[3 * ((j * imageHeight) + i) + 1] = currentPixelData[1];
				imagePixels[3 * ((j * imageHeight) + i) + 2] = currentPixelData[2];
			}
		}
		imagePixels = encryption(imagePixels,anAES);
		/* Encrypts the image to be hidden. */
		/**
		 * Width of the image where the other will be hidden.
		 */
		int placeWidth = placeToHide.getWidth();
		/**
		 * Height of the image where the other will be hidden.
		 */
		int placeHeight = placeToHide.getHeight();
		/* Variables used to move through the image where the other will be hidden. */
		for(int j = 0; j < placeWidth; j += 1){
			/* Controls movement through the image 
		   	horizontally. */
			for (int i = 0; i < placeHeight; i += 1){
				/* Controls movement through the image 
			   	vertically. */
				if((i == 0) && (j == 0)){
					placeToHide.setPixel(0,0,changeColor(placeToHide.getPixel(0,0),imageWidth));
				}
				/* Puts the width of the hidden image in the other image. */
				else if((i == 1) && (j == 0)){
					placeToHide.setPixel(0,1,changeColor(placeToHide.getPixel(0,1),imageHeight));
				}
				/* Puts the height of the hidden image in the other image. */
				else if((j * placeHeight) + i == imagePixels.length + 2){
					placeToHide.setPixel(j,i,changeColor(placeToHide.getPixel(j,i),423));
					/* Places 423 in the current pixel of the
					   image to indicate the end of the hidden image
					   has been reached. + 2 accounts for the dimensions
					   of the hidden image. */
					try{
						FileOutputStream output = new FileOutputStream(writeTo);
						placeToHide.compress(Bitmap.CompressFormat.PNG,0,output);
					}
					catch(Exception e){
						System.out.println(e);
					}
					/* Writes the new image to a File and displays it. */
					return placeToHide;
				}
				else{
					placeToHide.setPixel(j,i,changeColor(placeToHide.getPixel(j,i),imagePixels[(j * placeHeight) + i - 2]));
				}
				/* Places the number representing the color component of the hidden
				   image in a pixel of the other image. */
			}
		}
		return placeToHide;
		
	}
	
	/**
	 * Returns an array containing the red, green, and blue components of the given Color.
	 * 
	 * @param pixelColor Color of the current pixel.
	 * @return Integer array containing the red, green, and blue components of the given Color. 
	 */
	
	public static int[] getPixelData(int color){
		
		/**
		 * Contains the red, green, and blue components of the given Color.
		 */
		int[] pixelData = new int[3];
		pixelData[0] = Color.red(color);
		pixelData[1] = Color.green(color);	
		pixelData[2] = Color.blue(color);
		return pixelData;
		
	}
	
	/**
	 * Encrypts the information representing the secret message or image.
	 * 
	 * @param hiddenData Numbers representing the hidden .
	 * @param anAES Object used for Encryption.
	 * @return Encrypted representation of the hiddenData of the image to be hidden.
	 */
	
	public static int[] encryption(int[] hiddenData,AES anAES){
		
		/**
		 * Contains the encrypted information representing the hiddenData of the image to be hidden.
		 */
		int[] encrypted = null;
		if ((hiddenData.length % 16) == 0){
			encrypted = new int[hiddenData.length];
		}
		else{
			encrypted = new int[hiddenData.length + (16 - (hiddenData.length % 16))];
		}
		/* Encryption occurs 16 bytes at a time. Thus, the total number of encrypted bytes
		   must be a multiple of 16. */
		/**
		 * Temporarily holds encrypted bytes before they are added to the larger array.
		 * 
		 * Encryption occurs 16 bytes as a time. 
		 */
		byte[] encryptedBytes = new byte[16];
		for (int i = 0; i <= (hiddenData.length/16); i += 1){
			if((i == (hiddenData.length/16))){
				if(hiddenData.length % 16 != 0){
					/**
					 * Holds the last of the pixel data to be encrypted.
					 */
					byte[] lastPortion = convertToBytes(getPortion(hiddenData,(16 * i),((16 * i) + hiddenData.length % 16)));
					for (int j = 0; j < lastPortion.length; j += 1){
						encryptedBytes[j] = lastPortion[j];
					}
					/* Adds the last of the integer array to the byte array to be encrypted.  */
					for (int j = (hiddenData.length % 16); j < 16; j += 1){
						encryptedBytes[j] = (byte) ' ';
					}
					/* If the number of elements in the given integer array is not a
				   	multiple of 16, the extra bytes needed to reach 16 for the last
				   	part of encryption are added as the byte equivalent of blank
				   	spaces. */
					try{
						encryptedBytes = anAES.encrypt(encryptedBytes);
					}
					catch(Exception e){
						System.out.println(e);
					}
					/* Encrypts 16 bytes of the given integer array. */
				}
				else{
					return encrypted;
				}
			}
			else{
				try{
					encryptedBytes = anAES.encrypt(convertToBytes(getPortion(hiddenData,(16 * i),(16 * (i + 1)))));
				}
				catch(Exception e){
					System.out.println(e);
				}
				/* Encrypts 16 bytes of the given integer array. */
			}
			for(int j = 0; j < encryptedBytes.length; j += 1){
				if(encryptedBytes[j] >= 0){
					encrypted[(i * 16) + j] = (int) encryptedBytes[j];
				}
				else{
					encrypted[(i * 16 + j)] = (int) (encryptedBytes[j]) + 256;
				}
			}
			/* Adds the 16 encrypted bytes to the larger array holding all the
			   encrypted. */
		}
		return encrypted;
		
	}

	/**
	 * Decrypts the information representing the secret message or image.
	 * 
	 * @param encrypted Encrypted information representing the secret message or image.
	 * @param anAES Object used for Decryption.
	 * @return Decrypted information representing the secret message or image.
	 */
	
	public static int[] decryption(int[] encrypted,AES anAES){
		
		/**
		 * Holds the decrypted information representing the secret message or image.
		 */
		int[] decrypted = new int[encrypted.length];
		/**
		 * Temporarily holds decrypted bytes before they are added to the larger array. 
		 *
		 *  Decryption occurs 16 bytes at a time.
		 */
		byte[] decryptedBytes = new byte[16];
		for (int i = 0; i < (decrypted.length/16); i += 1){
			decryptedBytes = anAES.decrypt(convertToBytes(getPortion(encrypted,(16*i),(16*(i+1)))));
			/* Decrypts 16 bytes of the secret message or image. */
			for (int j = 0; j < 16; j += 1){
				if (decryptedBytes[j] >= 0){
					decrypted[(16 * i) + j] = (int) decryptedBytes[j];
				}
				else{
					decrypted[(16 * i) + j] = (int) (decryptedBytes[j]) + 256;
				}
			}
			/* Adds the 16 decrypted bytes to the larger array of all the decrypted information. */
		}
		return decrypted;
		
	}
	
	/**
	 * Converts an int array to a byte array.
	 * 
	 * @param ints An array of integers.
	 * @return Byte equivalent of the given integer array.
	 */
	
	public static byte[] convertToBytes(int[] ints){

		/**
		 * Byte equivalent of the given integer array. 
		 */
		byte[] bytes = new byte[ints.length];
		for (int i = 0; i < ints.length; i += 1){
			bytes[i] = (byte) ints[i];
		}
		return bytes;
		
	}
	
	/**
	 * Returns a portion of the given integer array.
	 * 
	 * Returned array does not include the given end index.
	 * 
	 * @param original An integer array.
	 * @param startIndex The first index of the given array to be included. 
	 * @param endIndex The index after the last index of the given array to be included.
	 * @return A portion of the given integer array.
	 */
	
	public static int[] getPortion(int[] original,int startIndex, int endIndex){
		
		/*
		 * Example:
		 * getPortion([1,2,3,4,5],1,4)
		 * return == [2,3,4]
		 */
		
		/**
		 * The portion of the given array to be returned.
		 */
		int[] portion = new int[endIndex - startIndex];
		for (int i = startIndex; i < endIndex; i += 1){
			portion[i - startIndex] = original[i];
		}
		return portion;
		
	}

	/**
	 * Returns a portion of the given byte array.
	 * 
	 * Returned array does not include the given end index.
	 * 
	 * @param original A byte array.
	 * @param startIndex The first index of the given array to be included. 
	 * @param endIndex The index of the given array after the last index to be included.
	 * @return A portion of the given integer array.
	 */
	
	public static byte[] getPortion(byte[] original,int startIndex, int endIndex){
		
		/*
		 * Example:
		 * getPortion([1,2,3,4,5],1,4)
		 * return == [2,3,4]
		 */
		
		/**
		 * The portion of the given array to be returned.
		 */
		byte[] portion = new byte[endIndex - startIndex];
		for (int i = startIndex; i < endIndex; i += 1){
			portion[i - startIndex] = original[i];
		}
		return portion;
		
	}
	
	/**
	 * Gets the hidden message or image from the given image.
	 *
	 * @param encoded File containing the encoded image.
	 * @param writeTo Location the new File should be written to.
	 * @param password Key required for decryption.
	 */
	
	public static Bitmap decode(String encoded, String writeTo, String password){
		
		/**
		 * The image containing the hidden message or image.
		 */
		Bitmap encodedImage = BitmapFactory.decodeFile(encoded);
		/* Gets a BufferedImage from the given File. */
		/**
		 * Holds the key for decryption.
		 */
		byte[] key = new byte[password.length()/2];
		for(int i = 0; i < 48; i += 2){
			key[i/2] = (byte) ((Character.digit(password.charAt(i), 16) << 4) + Character.digit(password.charAt(i+1), 16));
		}
		/* Gets the byte equivalent of the given password. */
		/**
		 * Object for decryption.
		 */
		AES anAES = new AES(key);
		if (checkText(encodedImage)){
			return decodeText(encodedImage,writeTo,anAES);
		}
		/* If an image contains a message, it is decoded as such. */
		else{
			return decodeImage(encodedImage,writeTo,anAES);
		}
		/* If an image does not contain a message, it is decoded as an image. */
		
	}
	
	/**
	 * Returns a boolean representing if a message is encoded in the image.
	 * 
	 * @param encodedImage Image to be checked for a hidden message.
	 * @return Whether or not the given image contains a message.
	 */
	
	public static boolean checkText(Bitmap encodedImage){
		
		/**
		 * Color of the pixel in the upper left corner of the given image.
		 */
		int pixelColor = encodedImage.getPixel(0,0);
		if (((Color.red(pixelColor) % 10) == 3) && ((Color.green(pixelColor) % 10) == 4) && ((Color.blue(pixelColor) % 10) == 2)){
			return true;
		}
		/* If the code 342 is in the upper left corner of the given image, the image
		   contains a hidden message. */
		else{
			return false;
		}
		
	}

	/**
	 * Gets the hidden message from an image.
	 * 
	 * Writes message to a text file.
	 * 
	 * @param encodedImage Image containing the hidden message.
	 * @param writeTo Location to write the new file.
	 * @param anAES Object used for Decryption.
	 */
	
	public static String decodeText(Bitmap encodedImage,String writeTo,AES anAES){
		
		/**
		 * Holds the hidden message.
		 */
		String message = "";
		/**
		 * Width of the image with the message in it.
		 */
		int width = encodedImage.getWidth();
		/**
		 * Height of the image with the message in it.
		 */
		int height = encodedImage.getHeight();
		/* Properties of the image used to move through
		   pixels of the image using a loop. */
		/**
		 * Holds the integers representing the hidden message.
		 */
		int[] messageNumbers = new int[width * height];
		/**
		 * Holds the integers representing the character in the current pixel.
		 */
		int[] currentPixel = new int[3];
		for (int j = 0; j < width; j += 1){
			/* Controls movement through the image horizontally. */
			for (int i = 0; i < height; i += 1){
				/* Controls movement through the image vertically. */
				if ((i == 0) && (j == 0)){
					/* Accounts for presence of code indicating a text message is
					   hidden within the image. */
				}
				else{
					currentPixel = getPixelDigits(encodedImage.getPixel(j,i));
					/* Gets the integers representing the character in the current pixel. */
					if((currentPixel[0] == 4) && (currentPixel[1] == 2) && (currentPixel[2] == 3)){
						/* Occurs when the end code 423 is found. */
						/**
						 * Combines 3 digits in messageNumbers into a single int. 
						 */
						int[] combinedMessage = new int[(j * height) + i - 1];
						for (int k = 0; k < combinedMessage.length; k +=1 ){
							combinedMessage[k] = digitsToInt(getPortion(messageNumbers,(3 * k),3 * (k + 1)));
						}
						combinedMessage = decryption(combinedMessage,anAES);
						/* Decrypts the integers representing the message. */
						message = convertToText(combinedMessage);
						/* Converts the integers representing the hidden message
						   to a string. */
						try{
							return message;
						}
						catch (Exception e){
							System.out.println(e);
						}
						return message;
					}
					else{
						messageNumbers[(3*((j*height)+i-1))] = currentPixel[0];
						messageNumbers[(3*((j*height)+i-1))+1] = currentPixel[1];
						messageNumbers[(3*((j*height)+i-1))+2] = currentPixel[2];
						/* Adds the integers representing the character in the
						   current pixel to the array of integers representing
						   the entire message. */
					}
				}
			}
		}
		return message;
		
	}
	
	/**
	 * Gets the image hidden in the given encoded image.
	 * 
	 * Writes hidden image to a png file.
	 * 
	 * @param encodedImage Image with another image hidden within it.
	 * @param writeTo Location to write the hidden image to.
	 * @param anAES Object used for Decryption.
	 */
	
	public static Bitmap decodeImage(Bitmap encodedImage,String writeTo,AES anAES){
		
		/**
		 * Width of the image with the hidden image within it.
		 */
		int encodedWidth = encodedImage.getWidth();
		/**
		 * Height of the image with the hidden image within it.
		 */
		int encodedHeight = encodedImage.getHeight();
		/* Properties of the encoded image used to move through
		   pixels of the image using a loop. */
		/**
		 * Width of the hidden image.
		 */
		int hiddenWidth = 0;
		/**
		 * Height of the hidden image.
		 */
		int hiddenHeight = 0;
		/* Properties of the hidden image used to move through 
		   pixels of the image using a loop. */
		/**
		 * Holds the integers representing the pixels of the hidden image.
		 */
		int[] hiddenPixels = null;
		/**
		 * Temporarily holds integer hidden within the current pixel.
		 */
		int pixelInt = 0;
		for (int j = 0; j < encodedWidth; j+= 1){
			/* Controls movement through the image horizontally. */
			for (int i = 0; i < encodedHeight; i += 1){
				/* Controls movement through the image vertically. */
				if ((i == 0) && (j == 0)){
					/* Gets width of hidden image. */
					hiddenWidth = getHiddenInt(encodedImage.getPixel(0,0));
				}
				else if ((j == 0) && (i == 1)){
					/* Gets height of hidden image. */
					hiddenHeight = getHiddenInt(encodedImage.getPixel(0,1));
					hiddenPixels = new int[3 * hiddenWidth * hiddenHeight];
				}
				else{
					pixelInt = getHiddenInt(encodedImage.getPixel(
							j,i));
					/* Gets the number hidden in the current pixel. */
					if (pixelInt == 423){
						/* Occurs when the end code is found. */
						hiddenPixels = decryption(hiddenPixels,anAES);
						/* Decrypts the numbers representing hidden image. */
						return rebuildImage(hiddenPixels,hiddenWidth,hiddenHeight,writeTo);
						/* Recreates the hidden image. */
					}
					else{
						hiddenPixels[(j * encodedHeight) + i - 2] = pixelInt;
						/* Puts the number representing the current pixel into the
						   larger array containing all the pixels' information. 
						   -2 accounts for the presence of the hidden image 
						   dimensions in the encoded image. */
					}
				}
			}
		}
		return null;
		
	}
	
	/**
	 * Gets the hidden code in the current pixel.
	 * 
	 * @param pixelColor Color of the current pixel.
	 * @return Hidden code in the current pixel.
	 */
	
	public static int[] getPixelDigits(int pixelColor){
		
		/*
		 * Example:
		 * getPixelDigits(new Color(131,222,43))
		 * return == [1,2,3]
		 */
		
		/**
		 * Holds the hidden code obtained from the current pixel.
		 */
		int[] pixelDigits = new int[3];
		pixelDigits[0] = intToDigits(Color.red(pixelColor))[2];
		pixelDigits[1] = intToDigits(Color.green(pixelColor))[2];
		pixelDigits[2] = intToDigits(Color.blue(pixelColor))[2];
		return pixelDigits;
		
	}
	
	/**
	 * Converts the integers representing the hidden message to text.
	 * 
	 * @param messageNumbers Integers representing the hidden message.
	 * @return The hidden message represented by the given integer array.
	 */
	
	public static String convertToText(int[] messageNumbers){
		
		/*
		 * Example:
		 * convertToText([065,066,067])
		 * return == "ABC"
		 */
		
		/**
		 * Hidden message.
		 */
		String message = "";
		for(int i = 0; i < messageNumbers.length; i += 1){
			message = message.concat(Character.toString((char) (messageNumbers[i])));
			/* Adds the current character to the end of the message. */
		}
		return message;
		
	}
	
	/**
	 * Creates an image from data about its pixels, width, and height.
	 * 
	 * @param pixels Integers representing the pixels in the hidden image.
	 * @param width Width of the hidden image.
	 * @param height Height of the hidden image.
	 * @param writeTo Location to write the hidden image to.
	 */
	
	public static Bitmap rebuildImage(int[] pixels,int width,int height,String writeTo){
		
		/**
		 * Hidden image.
		 */
		Bitmap hiddenImage = 
		try{
			FileOutputStream output = new FileOutputStream(writeTo);
			hiddenImage.compress(Bitmap.CompressFormat.PNG,0,output);
			/* Writes and displays the hidden image. */
		}
		catch(Exception e){
			System.out.println(e);
		}
		return hiddenImage;
		
	}
	
	/**
	 * Returns the integer hidden in the Color of a pixel.
	 * 
	 * @param pixelColor Color of the current pixel.
	 * @return Hidden code in the current pixel.
	 */
	
	public static int getHiddenInt(Color pixelColor){
		
		/*
		 * Example:
		 * getHiddenInt(new Color(131,142,203))
		 * return == 123
		 */
		
		/**
		 * Holds the digits of the hidden code in the current pixel. 
		 */
		int[] digits = new int[3];
		digits[0] = intToDigits(pixelColor.getRed())[2];
		digits[1] = intToDigits(pixelColor.getGreen())[2];
		digits[2] = intToDigits(pixelColor.getBlue())[2];
		return digitsToInt(digits);
		
	}
	
	/**
	 * Scales an image up by the given scale factor.
	 * 
	 * @param original Image to be scaled up.
	 * @param scaleFactor Amount the image should be scaled up by.
	 * @return Scaled up image.
	 */
	
	public static BufferedImage scaleUp(BufferedImage original,int scaleFactor){
		
		/**
		 * Scaled up image.
		 */
		BufferedImage scaledUp = new BufferedImage(original.getWidth() * scaleFactor,original.getHeight() * scaleFactor, BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < original.getHeight(); i += 1){
			/* Controls vertical movement through the image. */
			for (int j = 0; j < original.getWidth(); j += 1){
				/* Controls horizontal movement through the image. */
				for (int k = 0; k < scaleFactor; k += 1){
					for (int l = 0; l < scaleFactor; l += 1){
						scaledUp.setRGB((j*scaleFactor)+k,(i*scaleFactor)+l,original.getRGB(j,i));
					}
				}
				/* Converts a (scaleFactor * width) x (scaleFactor * height) area of 
				   pixels in the new image to match a single pixel in the original 
				   image. */
			}
		}
		return scaledUp;
		
	}
	
	/**
	 * Changes the given Color to hold the given number.
	 * 
	 * @param currentColor A color for the number to be hidden in.
	 * @param inputNumber The number to be hidden in the Color.
	 * @return Given Color with the given number inside of it.
	 */
	
	public static Color changeColor(Color currentColor,int inputNumber){
		
		/*
		 * Example:
		 * changeColor(Color(133,144,155),236)
		 * return == Color(132,143,156)
		 */
		
		/**
		 * Red component of the given color.
		 */
		int[] red = intToDigits(currentColor.getRed());
		/**
		 * Green component of the given color.
		 */
		int[] green = intToDigits(currentColor.getGreen());
		/**
		 * Blue component of the given color.
		 */
		int[] blue = intToDigits(currentColor.getBlue());
		/**
		 * Digits of the given number.
		 */
		int[] number = intToDigits(inputNumber);
		
		
		if ((blue[0] == 2) && (blue[1] == 5) && (number[2] > 5)){
			blue[1] = 4;
		}
		/* Accounts for the fact that blue can not exceed
		   255. */
		else if((number[2] - blue[2]) > 5){
			if ((blue[0] != 0) || (blue[1] != 0)){
				blue[1] -= 1;
			}
		}
		/* If the number to put in is 5 greater than the current last digit,
		   reduce the second digit by 1. */
		else if(blue[2] - number[2] > 5){
			blue[1] += 1;
		}
		/* If the current last digit is 5 greater than the digit to be put in,
		   increase the second digit by 1. */
		blue[2] = number[2];
		/* Places the third digit representing the input
		   number as the last digit of the blue component
		   of the pixel. */
		
		if ((green[0] == 2) && (green[1] == 5) && (number[1] > 5)){
			green[1] = 4;
		}
		/* Accounts for the fact that green can not exceed
		   255. */
		else if((number[1] - green[2]) > 5){
			if ((green[0] != 0) || (green[1] != 0)){
				green[1] -= 1;
			}
		}
		/* If the number to put in is 5 greater than the current last digit,
		   reduce the second digit by 1. */
		else if(green[2] - number[1] > 5){
			green[1] += 1;
		}
		/* If the current last digit is 5 greater than the digit to be put in,
		   increase the second digit by 1. */
		green[2] = number[1];
		/* Places the second digit representing the input
		   number as the last digit of the green component
		   of the pixel. */
		
		if ((red[0] == 2) && (red[1] == 5) && (number[0] > 5)){
			red[1] = 4;
		}
		/* Accounts for the fact that red can not exceed
		   255. */
		else if((number[0] - red[2]) > 5){
			if ((red[0] != 0) || (red[1] != 0)){
				red[1] -= 1;
			}
		}
		/* If the number to put in is 5 greater than the current last digit,
		   reduce the second digit by 1. */
		else if(red[2] - number[0] > 5){
			red[1] += 1;
		}
		/* If the current last digit is 5 greater than the digit to be put in,
		   increase the second digit by 1. */
		red[2] = number[0];
		/* Places the first digit representing the input
		   number as the last digit of the red component
		   of the pixel. */
		
		return new Color(digitsToInt(red),digitsToInt(green),
				digitsToInt(blue));
		
	}
	
	/**
	 * Converts an integer to its digits.
	 * 
	 * Integer must be 3 digit or less.
	 * 
	 * @param aNumber Integer to be converted to its digits.
	 * @return Digits of the given integer.
	 */
	
	public static int[] intToDigits(int aNumber){
		
		/*
		 * Example:
		 * intToDigits(137)
		 * return == [1,3,7]
		 * intToDigits(53)
		 * return == [0,5,3]
		 */
		
		/**
		 * Digits of the given number.
		 */
		int[] digits = new int[3];
		digits[0] = aNumber / 100;
		/* The integer component of a three-digit number divided 
		   by 100 reflects the first digit of that number. */
		digits[1] = (aNumber % 100) / 10;
		/* A number mod 100 reflects the last two digits
		   of that number.  The integer component of a two-digit
		   number divided by 10 reflects the first digit of
		   that number. */
		digits[2] = aNumber % 10;
		/* A number mod 10 reflects the last digit of a 
		   number. */
		return digits;
	
	}
	
	/**
	 * Converts digits into an integer.
	 * 
	 * Must provide 3 digits.
	 * 
	 * @param digits Digits to be converted to an integer.
	 * @return Integer represented by the given digits.
	 */
	
	public static int digitsToInt(int[] digits){
		
		/*
		 * Example:
		 * digitsToInt([1,3,7])
		 * return == 137
		 * digitsToInt([0,5,3])
		 * return == 53
		 */
		
		/* The first integer in the array represents the digit
		   in the hundreds place of the final integer.
		   The second integer in the array represents the digit
		   in the tens place of the final integer.
		   The third integer in the array represents the digit
		   in the ones place of the final integer. */
		return (digits[0] * 100) + (digits[1] * 10) + digits[2];
		
	}
	
	/**
	 * Displays the given image and the encryption key.
	 * 
	 * @param anImage Image to be displayed.
	 * @param anAES Object used for Encryption.
	 */
	public static void showImage(BufferedImage anImage,AES anAES){

		/**
		 * Used to display the given image.
		 */
		JFrame frame = new JFrame("Drawing Frame");
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    /**
	     * Used to display the given image.
	     */
	    MyCanvas canvas = new MyCanvas(anImage);
	    /**
	     * Used to display the given image.
	     */
	    JPanel panel = new JPanel();
	    panel.add(canvas);
	    panel.add(new JTextArea(AES.arrayToString(anAES.key)));
	    panel.setLayout(new GridLayout());
	    frame.getContentPane().add(panel);
	    frame.setSize(anImage.getWidth() + 500,anImage.getHeight() + 100);
	    frame.setVisible(true);
	    /* Displays the given image. */
	    return;
		
	}

/**
 * Displays the given image.
 * 
 * @param anImage Image to be displayed.
 */
public static void showImage(BufferedImage anImage){

	/**
	 * Used to display the given image.
	 */
	JFrame frame = new JFrame("Drawing Frame");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    MyCanvas canvas = new MyCanvas(anImage);
    frame.getContentPane().add(canvas);
    frame.setSize(anImage.getWidth(),anImage.getHeight());
    frame.setVisible(true);
    /* Displays the given image. */
    return;
	
	}

}

class MyCanvas extends JComponent{
	
	BufferedImage image;
	
	/**
	 * Constructor for the MyCanvas class.
	 * 
	 * @param anImage Image to be displayed.
	 */
	
	public MyCanvas(BufferedImage anImage){
		
		image = anImage;
		
	}
	
	public void paint(Graphics g){
	
		Graphics2D g2d = (Graphics2D) g;
		try{
			g2d.drawImage(image,null,1,1);
		}
		catch(Exception e){
			System.out.println(e);
		}
		
	}
	
}
