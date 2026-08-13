import { NextRequest, NextResponse } from "next/server";
import { db } from "@/db";
import { qrCodes } from "@/db/schema";
import { getCurrentUserId } from "@/lib/auth";
import { eq, desc } from "drizzle-orm";
import QRCode from "qrcode";

export async function GET() {
  try {
    const userId = await getCurrentUserId();
    if (!userId) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const codes = await db
      .select()
      .from(qrCodes)
      .where(eq(qrCodes.userId, userId))
      .orderBy(desc(qrCodes.createdAt));

    return NextResponse.json({ qrCodes: codes });
  } catch (error) {
    console.error("Error fetching QR codes:", error);
    return NextResponse.json(
      { error: "Internal server error" },
      { status: 500 }
    );
  }
}

export async function POST(req: NextRequest) {
  try {
    const userId = await getCurrentUserId();
    if (!userId) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const body = await req.json();
    const {
      title,
      targetUrl,
      description,
      fgColor = "#000000",
      bgColor = "#FFFFFF",
      style = "square",
      size = 300,
      category,
    } = body;

    if (!title || !targetUrl) {
      return NextResponse.json(
        { error: "Title and target URL are required" },
        { status: 400 }
      );
    }

    const qrDataUrl = await QRCode.toDataURL(targetUrl, {
      width: size,
      color: { dark: fgColor, light: bgColor },
      margin: 2,
      errorCorrectionLevel: "H",
    });

    const [code] = await db
      .insert(qrCodes)
      .values({
        userId,
        title,
        targetUrl,
        description,
        qrDataUrl,
        fgColor,
        bgColor,
        style,
        size,
        category,
      })
      .returning();

    return NextResponse.json({ qrCode: code }, { status: 201 });
  } catch (error) {
    console.error("Error creating QR code:", error);
    return NextResponse.json(
      { error: "Internal server error" },
      { status: 500 }
    );
  }
}
