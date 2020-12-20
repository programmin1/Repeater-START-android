package com.hearham.repeaterstart;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.regex.Pattern;
// Copyright (c) 2011, Yves Goergen, http://unclassified.software/source/maidenheadlocator
//
// Copying and distribution of this file, with or without modification, are permitted provided the
// copyright notice and this notice are preserved. This file is offered as-is, without any warranty.

// This class is based on a Perl module by Dirk Koopman, G1TLH, from 2002-11-07.
// Source: http://www.koders.com/perl/fidDAB6FD208AC4F5C0306CA344485FD0899BD2F328.aspx

/// <summary>
/// Class providing static methods for calculating with Maidenhead locators, especially
/// distance and bearing.
/// Based on the code from https://unclassified.software/en/source/maidenheadlocator
/// </summary>
public class MaidenheadLocator
{
	/// <summary>
	/// Convert a locator to latitude and longitude in degrees
	/// </summary>
	/// <param name="locator">Locator string to convert</param>
	/// <returns>LatLng structure</returns>
	public static LatLng LocatorToLatLng(String locator)
	{
		locator = locator.trim().toUpperCase();
		if (Pattern.matches("^[A-R]{2}[0-9]{2}$", locator))
		{
			LatLng ll = new LatLng();
			ll.setLongitude( (locator.charAt(0) - 'A') * 20 + (locator.charAt(2) - '0' + 0.5) * 2 - 180 );
			ll.setLatitude( (locator.charAt(1) - 'A') * 10 + (locator.charAt(3) - '0' + 0.5) - 90 );
			return ll;
		}
		else if (Pattern.matches( "^[A-R]{2}[0-9]{2}[A-X]{2}$", locator))
		{
			LatLng ll = new LatLng();
			ll.setLongitude( (locator.charAt(0) - 'A') * 20 + (locator.charAt(2) - '0') * 2 + (locator.charAt(4) - 'A' + 0.5) / 12 - 180 );
			ll.setLatitude( (locator.charAt(1) - 'A') * 10 + (locator.charAt(3) - '0') + (locator.charAt(5) - 'A' + 0.5) / 24 - 90 );
			return ll;
		}
		else if (Pattern.matches( "^[A-R]{2}[0-9]{2}[A-X]{2}[0-9]{2}$", locator))
		{
			LatLng ll = new LatLng();
			ll.setLongitude( (locator.charAt(0) - 'A') * 20 + (locator.charAt(2) - '0') * 2 + (locator.charAt(4) - 'A' + 0.0) / 12 + (locator.charAt(6) - '0' + 0.5) / 120 - 180 );
			ll.setLatitude( (locator.charAt(1) - 'A') * 10 + (locator.charAt(3) - '0') + (locator.charAt(5) - 'A' + 0.0) / 24 + (locator.charAt(7) - '0' + 0.5) / 240 - 90 );
			return ll;
		}
		else if (Pattern.matches( "^[A-R]{2}[0-9]{2}[A-X]{2}[0-9]{2}[A-X]{2}$", locator))
		{
			LatLng ll = new LatLng();
			ll.setLongitude( (locator.charAt(0) - 'A') * 20 + (locator.charAt(2) - '0') * 2 + (locator.charAt(4) - 'A' + 0.0) / 12 + (locator.charAt(6) - '0' + 0.0) / 120 + (locator.charAt(8) - 'A' + 0.5) / 120 / 24 - 180 );
			ll.setLatitude( (locator.charAt(1) - 'A') * 10 + (locator.charAt(3) - '0') + (locator.charAt(5) - 'A' + 0.0) / 24 + (locator.charAt(7) - '0' + 0.0) / 240 + (locator.charAt(9) - 'A' + 0.5) / 240 / 24 - 90 );
			return ll;
		}
		else
		{
			return null;
		}
	}

	/*TODO port the rest from CSharp

	/// <summary>
	/// Convert latitude and longitude in degrees to a locator
	/// </summary>
	/// <param name="ll">LatLng structure to convert</param>
	/// <returns>Locator string</returns>
	public static string LatLngToLocator(LatLng ll)
	{
		return LatLngToLocator(ll.Lat, ll.Long, 0);
	}

	/// <summary>
	/// Convert latitude and longitude in degrees to a locator
	/// </summary>
	/// <param name="ll">LatLng structure to convert</param>
	/// <param name="Ext">Extra precision (0, 1, 2)</param>
	/// <returns>Locator string</returns>
	public static string LatLngToLocator(LatLng ll, int Ext)
	{
		return LatLngToLocator(ll.Lat, ll.Long, Ext);
	}

	/// <summary>
	/// Convert latitude and longitude in degrees to a locator
	/// </summary>
	/// <param name="Lat">Latitude to convert</param>
	/// <param name="Long">Longitude to convert</param>
	/// <returns>Locator string</returns>
	public static string LatLngToLocator(double Lat, double Long)
	{
		return LatLngToLocator(Lat, Long, 0);
	}

	/// <summary>
	/// Convert latitude and longitude in degrees to a locator
	/// </summary>
	/// <param name="Lat">Latitude to convert</param>
	/// <param name="Long">Longitude to convert</param>
	/// <param name="Ext">Extra precision (0, 1, 2)</param>
	/// <returns>Locator string</returns>
	public static string LatLngToLocator(double Lat, double Long, int Ext)
	{
		int v;
		string locator = "";

		Lat += 90;
		Long += 180;

		locator += (char) ('A' + Math.Floor(Long / 20));
		locator += (char) ('A' + Math.Floor(Lat / 10));
		Long = Math.IEEERemainder(Long, 20);
		if (Long < 0) Long += 20;
		Lat = Math.IEEERemainder(Lat, 10);
		if (Lat < 0) Lat += 10;

		locator += (char) ('0' + Math.Floor(Long / 2));
		locator += (char) ('0' + Math.Floor(Lat / 1));
		Long = Math.IEEERemainder(Long, 2);
		if (Long < 0) Long += 2;
		Lat = Math.IEEERemainder(Lat, 1);
		if (Lat < 0) Lat += 1;

		locator += (char) ('A' + Math.Floor(Long * 12));
		locator += (char) ('A' + Math.Floor(Lat * 24));
		Long = Math.IEEERemainder(Long, (double) 1 / 12);
		if (Long < 0) Long += (double) 1 / 12;
		Lat = Math.IEEERemainder(Lat, (double) 1 / 24);
		if (Lat < 0) Lat += (double) 1 / 24;

		if (Ext >= 1)
		{
			locator += (char) ('0' + Math.Floor(Long * 120));
			locator += (char) ('0' + Math.Floor(Lat * 240));
			Long = Math.IEEERemainder(Long, (double) 1 / 120);
			if (Long < 0) Long += (double) 1 / 120;
			Lat = Math.IEEERemainder(Lat, (double) 1 / 240);
			if (Lat < 0) Lat += (double) 1 / 240;
		}

		if (Ext >= 2)
		{
			locator += (char) ('A' + Math.Floor(Long * 120 * 24));
			locator += (char) ('A' + Math.Floor(Lat * 240 * 24));
			Long = Math.IEEERemainder(Long, (double) 1 / 120 / 24);
			if (Long < 0) Long += (double) 1 / 120 / 24;
			Lat = Math.IEEERemainder(Lat, (double) 1 / 240 / 24);
			if (Lat < 0) Lat += (double) 1 / 240 / 24;
		}

		return locator;

		//Lat += 90;
		//Long += 180;
		//v = (int) (Long / 20);
		//Long -= v * 20;
		//locator += (char) ('A' + v);
		//v = (int) (Lat / 10);
		//Lat -= v * 10;
		//locator += (char) ('A' + v);
		//locator += ((int) (Long / 2)).ToString();
		//locator += ((int) Lat).ToString();
		//Long -= (int) (Long / 2) * 2;
		//Lat -= (int) Lat;
		//locator += (char) ('A' + Long * 12);
		//locator += (char) ('A' + Lat * 24);
		//return locator;
	}

	/// <summary>
	/// Convert radians to degrees
	/// </summary>
	/// <param name="rad"></param>
	/// <returns></returns>
	public static double RadToDeg(double rad)
	{
		return rad / Math.PI * 180;
	}

	/// <summary>
	/// Convert degrees to radians
	/// </summary>
	/// <param name="deg"></param>
	/// <returns></returns>
	public static double DegToRad(double deg)
	{
		return deg / 180 * Math.PI;
	}

	/// <summary>
	/// Calculate the distance in km between two locators
	/// </summary>
	/// <param name="A">Start locator string</param>
	/// <param name="B">End locator string</param>
	/// <returns>Distance in km</returns>
	public static double Distance(string A, string B)
	{
		return Distance(LocatorToLatLng(A), LocatorToLatLng(B));
	}

	/// <summary>
	/// Calculate the distance in km between two locators
	/// </summary>
	/// <param name="A">Start LatLng structure</param>
	/// <param name="B">End LatLng structure</param>
	/// <returns>Distance in km</returns>
	public static double Distance(LatLng A, LatLng B)
	{
		if (A.CompareTo(B) == 0) return 0;

		double hn = DegToRad(A.Lat);
		double he = DegToRad(A.Long);
		double n = DegToRad(B.Lat);
		double e = DegToRad(B.Long);

		double co = Math.Cos(he - e) * Math.Cos(hn) * Math.Cos(n) + Math.Sin(hn) * Math.Sin(n);
		double ca = Math.Atan(Math.Abs(Math.Sqrt(1 - co * co) / co));
		if (co < 0) ca = Math.PI - ca;
		double dx = 6367 * ca;

		return dx;
	}

	/// <summary>
	/// Calculate the azimuth in degrees between two locators
	/// </summary>
	/// <param name="A">Start locator string</param>
	/// <param name="B">End locator string</param>
	/// <returns>Azimuth in degrees</returns>
	public static double Azimuth(string A, string B)
	{
		return Azimuth(LocatorToLatLng(A), LocatorToLatLng(B));
	}

	/// <summary>
	/// Calculate the azimuth in degrees between two locators
	/// </summary>
	/// <param name="A">Start LatLng structure</param>
	/// <param name="B">End LatLng structure</param>
	/// <returns>Azimuth in degrees</returns>
	public static double Azimuth(LatLng A, LatLng B)
	{
		if (A.CompareTo(B) == 0) return 0;

		double hn = DegToRad(A.Lat);
		double he = DegToRad(A.Long);
		double n = DegToRad(B.Lat);
		double e = DegToRad(B.Long);

		double co = Math.Cos(he - e) * Math.Cos(hn) * Math.Cos(n) + Math.Sin(hn) * Math.Sin(n);
		double ca = Math.Atan(Math.Abs(Math.Sqrt(1 - co * co) / co));
		if (co < 0) ca = Math.PI - ca;

		double si = Math.Sin(e - he) * Math.Cos(n) * Math.Cos(hn);
		co = Math.Sin(n) - Math.Sin(hn) * Math.Cos(ca);
		double az = Math.Atan(Math.Abs(si / co));
		if (co < 0) az = Math.PI - az;
		if (si < 0) az = -az;
		if (az < 0) az = az + 2 * Math.PI;

		return RadToDeg(az);
	}
}

	/// <summary>
	/// Simple structure to store a position in latitude and longitude
	/// </summary>
	public struct LatLng : IComparable
		{
/// <summary>
/// Latitude, -90 to +90 (N/S direction)
/// </summary>
public double Lat;
/// <summary>
/// Longitude, -180 to +180 (W/E direction)
/// </summary>
public double Long;

public override string ToString()
		{
		return Long.ToString("#.###") + (Long >= 0 ? "N" : "S") + " " + Lat.ToString("#.###") + (Lat >= 0 ? "E" : "W");
		}

public int CompareTo(object to)
		{
		if (to is LatLng)
		{
		if (Lat == ((LatLng) to).Lat && Long == ((LatLng) to).Long) return 0;
		return -1;
		}
		return -1;
		}
		}*/
		}
