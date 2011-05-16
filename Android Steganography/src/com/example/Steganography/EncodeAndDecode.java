package com.example.Steganography;
import java.io.FileOutputStream;
import android.graphics.BitmapFactory.Options;
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
	 */
	
public static TwoReturn encodeText(String original,String writeTo,String message){
		
		
		/**
		 * Holds the image to be encoded into.
		 */
		Bitmap immutable = BitmapFactory.decodeFile(original);
		Bitmap encodedImage = immutable.copy(Bitmap.Config.ARGB_8888, true);
		
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
		byte[] key = AES.generateKey();
		/* Converts each message character to an integer. */
		//messageData = convertToInt(AES.encrypt(key,convertToBytes(messageData)));
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
						output.close();
					}
					catch (Exception e){
						System.out.println(e);
					}
					/* Writes encoded image to a file and displays it. */
					return new TwoReturn(encodedImage,key);
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
		return null;
		
	}

	/**
	 * Encodes an image in another image.
	 * 
	 * Writes encoded image to a png file.
	 * 
	 * @param hideThis Pathway of the file containing image to be hidden.
	 * @param hideIn Pathway of the file containing image that will contain other image.
	 * @param writeTo Pathway of the file where the new image should be written to.
	 */

	public static TwoReturn encodePicture(String hideThis,String hideIn,String writeTo){
	
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
		byte[] key = AES.generateKey();
		imagePixels = convertToInt(AES.encrypt(key,convertToBytes(imagePixels)));
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
						output.close();
					}
					catch(Exception e){
						System.out.println(e);
					}
					/* Writes the new image to a File and displays it. */
					return new TwoReturn(placeToHide,key);
				}
				else{
					placeToHide.setPixel(j,i,changeColor(placeToHide.getPixel(j,i),imagePixels[(j * placeHeight) + i - 2]));
				}
				/* Places the number representing the color component of the hidden
				   image in a pixel of the other image. */
			}
		}
		return null;
		
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
	 * Converts a byte array to an int array.
	 * 
	 * @param bytes An array of bytes.
	 * @return Int equivalent of the given byte array.
	 */
	
	public static int[] convertToInt(byte[] bytes){

		/**
		 * Int equivalent of the given byte array. 
		 */
		int[] ints = new int[bytes.length];
		for (int i = 0; i < ints.length; i += 1){
			ints[i] = (int) bytes[i];
		}
		return ints;
		
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
	 * @param password Object used for encryption.
	 */
	
	public static String decodeText(String encoded,String password){
		
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
						combinedMessage = convertToInt(AES.decrypt(key,convertToBytes(combinedMessage)));
						/* Decrypts the integers representing the message. */
						message = convertToText(combinedMessage);
						/* Converts the integers representing the hidden message
						   to a string. */
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
	 * @param password Object used for Decryption.
	 */
	
	public static Bitmap decodeImage(String encoded,String writeTo,String password){
		
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
						hiddenPixels = convertToInt(AES.decrypt(key,convertToBytes(hiddenPixels)));
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
		 * Ints representing the colors of the pixels in the hidden image.
		 */
		int[] color  = new int[pixels.length/3];
		for (int i = 0; i < color.length; i += 1){
			color[i] = Color.rgb(pixels[(3 * i)],pixels[(3 * i) + 1],pixels[(3 * i) + 2]);
		}
		/**
		 * Hidden image.
		 */
		Bitmap hiddenImage = Bitmap.createBitmap(color,width,height,Bitmap.Config.RGB_565);
		try{
			FileOutputStream output = new FileOutputStream(writeTo);
			hiddenImage.compress(Bitmap.CompressFormat.PNG,0,output);
			/* Writes and displays the hidden image. */
			output.close();
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
	
	public static int getHiddenInt(int pixelColor){
		
		/**
		 * Holds the digits of the hidden code in the current pixel. 
		 */
		int[] digits = new int[3];
		digits[0] = intToDigits(Color.red(pixelColor))[2];
		digits[1] = intToDigits(Color.green(pixelColor))[2];
		digits[2] = intToDigits(Color.blue(pixelColor))[2];
		return digitsToInt(digits);
		
	}
	
	/**
	 * Scales an image up by the given scale factor.
	 * 
	 * @param original Image to be scaled up.
	 * @param scaleFactor Amount the image should be scaled up by.
	 * @return Scaled up image.
	 */
	
	public static Bitmap scaleUp(Bitmap original,int scaleFactor){
		
		/**
		 * Colors of the pixels 
		 */
		int[] color = new int[scaleFactor * scaleFactor * original.getWidth() * original.getHeight()];
		for (int i = 0; i < original.getHeight(); i += 1){
			/* Controls vertical movement through the image. */
			for (int j = 0; j < original.getWidth(); j += 1){
				/* Controls horizontal movement through the image. */
				for (int k = 0; k < scaleFactor; k += 1){
					for (int l = 0; l < scaleFactor; l += 1){
						color[(((i * scaleFactor) + k) * original.getWidth()) + (j * scaleFactor) + l] = original.getPixel(j,i);
					}
				}
				/* Converts a (scaleFactor * width) x (scaleFactor * height) area of 
				   pixels in the new image to match a single pixel in the original 
				   image. */
			}
		}
		return Bitmap.createBitmap(color,scaleFactor * original.getWidth(),scaleFactor * original.getHeight(),Bitmap.Config.RGB_565);
		
	}
	
	/**
	 * Changes the given Color to hold the given number.
	 * 
	 * @param currentColor A color for the number to be hidden in.
	 * @param inputNumber The number to be hidden in the Color.
	 * @return Given Color with the given number inside of it.
	 */
	
	public static int changeColor(int currentColor,int inputNumber){
		
		/**
		 * Red component of the given color.
		 */
		int[] red = intToDigits(Color.red(currentColor));
		/**
		 * Green component of the given color.
		 */
		int[] green = intToDigits(Color.green(currentColor));
		/**
		 * Blue component of the given color.
		 */
		int[] blue = intToDigits(Color.blue(currentColor));
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
		
		return Color.rgb(digitsToInt(red),digitsToInt(green),
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

}
