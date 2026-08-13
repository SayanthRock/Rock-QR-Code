import { NextResponse } from "next/server";
import { db } from "@/db";
import { users, qrCodes, scanEvents, folders } from "@/db/schema";
import { hashPassword, createToken } from "@/lib/auth";
import { eq } from "drizzle-orm";
import QRCode from "qrcode";

export async function POST() {
  try {
    // Check if demo user already exists
    const existing = await db
      .select()
      .from(users)
      .where(eq(users.email, "demo@rockqr.io"))
      .limit(1);

    if (existing.length > 0) {
      const token = await createToken(existing[0].id);
      const response = NextResponse.json({
        message: "Demo data already seeded",
        user: { id: existing[0].id, name: existing[0].name, email: existing[0].email },
      });
      response.cookies.set("auth_token", token, {
        httpOnly: true,
        secure: process.env.NODE_ENV === "production",
        sameSite: "lax",
        maxAge: 7 * 24 * 60 * 60,
        path: "/",
      });
      return response;
    }

    const passwordHash = await hashPassword("demo1234");

    const [demoUser] = await db
      .insert(users)
      .values({
        name: "Alex Rivera",
        email: "demo@rockqr.io",
        passwordHash,
      })
      .returning();

    // Create folders
    await db.insert(folders).values([
      { userId: demoUser.id, name: "Marketing", color: "#ef4444" },
      { userId: demoUser.id, name: "Products", color: "#3b82f6" },
      { userId: demoUser.id, name: "Events", color: "#10b981" },
      { userId: demoUser.id, name: "Social Media", color: "#f59e0b" },
    ]);

    const qrEntries = [
      {
        title: "Company Website",
        targetUrl: "https://rockqr.io",
        description: "Main company landing page QR code for business cards",
        fgColor: "#1e1b4b",
        bgColor: "#FFFFFF",
        category: "Marketing",
        scanCount: 1847,
      },
      {
        title: "Product Catalog 2026",
        targetUrl: "https://rockqr.io/catalog/2026",
        description: "Spring collection product catalog",
        fgColor: "#0f172a",
        bgColor: "#f8fafc",
        category: "Products",
        scanCount: 923,
      },
      {
        title: "LinkedIn Profile",
        targetUrl: "https://linkedin.com/in/alexrivera",
        description: "Professional LinkedIn profile link",
        fgColor: "#0077b5",
        bgColor: "#FFFFFF",
        category: "Social Media",
        scanCount: 456,
      },
      {
        title: "Tech Summit 2026 — Registration",
        targetUrl: "https://events.rockqr.io/techsummit2026",
        description: "Registration page for annual tech summit event",
        fgColor: "#7c3aed",
        bgColor: "#faf5ff",
        category: "Events",
        scanCount: 2103,
      },
      {
        title: "Restaurant Menu",
        targetUrl: "https://rockqr.io/menu/bistro-modern",
        description: "Digital menu for Bistro Modern restaurant",
        fgColor: "#dc2626",
        bgColor: "#FFFFFF",
        category: "Products",
        scanCount: 3542,
      },
      {
        title: "App Download Link",
        targetUrl: "https://rockqr.io/app/download",
        description: "Direct download link for Rock QR mobile app",
        fgColor: "#059669",
        bgColor: "#ecfdf5",
        category: "Marketing",
        scanCount: 1256,
      },
      {
        title: "YouTube Channel",
        targetUrl: "https://youtube.com/@rockqrstudio",
        description: "Subscribe to our YouTube channel",
        fgColor: "#dc2626",
        bgColor: "#FFFFFF",
        category: "Social Media",
        scanCount: 789,
      },
      {
        title: "Wi-Fi Guest Access",
        targetUrl: "WIFI:T:WPA;S:OfficeGuest;P:Welcome2026;;",
        description: "Guest Wi-Fi access QR code for office visitors",
        fgColor: "#1e40af",
        bgColor: "#FFFFFF",
        category: "Marketing",
        scanCount: 567,
      },
    ];

    for (const entry of qrEntries) {
      const qrDataUrl = await QRCode.toDataURL(entry.targetUrl, {
        width: 300,
        color: { dark: entry.fgColor, light: entry.bgColor },
        margin: 2,
        errorCorrectionLevel: "H",
      });

      const [code] = await db
        .insert(qrCodes)
        .values({
          userId: demoUser.id,
          title: entry.title,
          targetUrl: entry.targetUrl,
          description: entry.description,
          qrDataUrl,
          fgColor: entry.fgColor,
          bgColor: entry.bgColor,
          scanCount: entry.scanCount,
          category: entry.category,
        })
        .returning();

      // Create some scan events
      const scanCount = Math.min(entry.scanCount, 15);
      const countries = ["United States", "United Kingdom", "Germany", "Japan", "Brazil", "Australia", "Canada", "France"];
      const cities = ["New York", "London", "Berlin", "Tokyo", "São Paulo", "Sydney", "Toronto", "Paris"];

      for (let i = 0; i < scanCount; i++) {
        const daysAgo = Math.floor(Math.random() * 30);
        const date = new Date();
        date.setDate(date.getDate() - daysAgo);
        const ci = Math.floor(Math.random() * countries.length);

        await db.insert(scanEvents).values({
          qrCodeId: code.id,
          country: countries[ci],
          city: cities[ci],
          userAgent: "Mozilla/5.0 (iPhone; CPU iPhone OS 18_0)",
          ipAddress: `${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}`,
          scannedAt: date,
        });
      }
    }

    const token = await createToken(demoUser.id);

    const response = NextResponse.json({
      message: "Demo data seeded successfully",
      user: { id: demoUser.id, name: demoUser.name, email: demoUser.email },
    });

    response.cookies.set("auth_token", token, {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "lax",
      maxAge: 7 * 24 * 60 * 60,
      path: "/",
    });

    return response;
  } catch (error) {
    console.error("Seed error:", error);
    return NextResponse.json(
      { error: "Failed to seed data" },
      { status: 500 }
    );
  }
}
