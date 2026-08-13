import { NextRequest, NextResponse } from "next/server";
import { db } from "@/db";
import { qrCodes } from "@/db/schema";
import { getCurrentUserId } from "@/lib/auth";
import { eq, and } from "drizzle-orm";
import QRCode from "qrcode";

export async function GET(
  _req: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const userId = await getCurrentUserId();
    if (!userId) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const { id } = await params;

    const [code] = await db
      .select()
      .from(qrCodes)
      .where(and(eq(qrCodes.id, id), eq(qrCodes.userId, userId)))
      .limit(1);

    if (!code) {
      return NextResponse.json({ error: "Not found" }, { status: 404 });
    }

    return NextResponse.json({ qrCode: code });
  } catch (error) {
    console.error("Error fetching QR code:", error);
    return NextResponse.json(
      { error: "Internal server error" },
      { status: 500 }
    );
  }
}

export async function PUT(
  req: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const userId = await getCurrentUserId();
    if (!userId) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const { id } = await params;
    const body = await req.json();
    const { title, targetUrl, description, fgColor, bgColor, style, size, category, isActive } = body;

    let qrDataUrl: string | undefined;
    if (targetUrl || fgColor || bgColor || size) {
      const existing = await db
        .select()
        .from(qrCodes)
        .where(and(eq(qrCodes.id, id), eq(qrCodes.userId, userId)))
        .limit(1);

      if (existing.length === 0) {
        return NextResponse.json({ error: "Not found" }, { status: 404 });
      }

      const code = existing[0];
      qrDataUrl = await QRCode.toDataURL(targetUrl || code.targetUrl, {
        width: size || code.size,
        color: {
          dark: fgColor || code.fgColor,
          light: bgColor || code.bgColor,
        },
        margin: 2,
        errorCorrectionLevel: "H",
      });
    }

    const updateData: Record<string, unknown> = { updatedAt: new Date() };
    if (title !== undefined) updateData.title = title;
    if (targetUrl !== undefined) updateData.targetUrl = targetUrl;
    if (description !== undefined) updateData.description = description;
    if (fgColor !== undefined) updateData.fgColor = fgColor;
    if (bgColor !== undefined) updateData.bgColor = bgColor;
    if (style !== undefined) updateData.style = style;
    if (size !== undefined) updateData.size = size;
    if (category !== undefined) updateData.category = category;
    if (isActive !== undefined) updateData.isActive = isActive;
    if (qrDataUrl) updateData.qrDataUrl = qrDataUrl;

    const [updated] = await db
      .update(qrCodes)
      .set(updateData)
      .where(and(eq(qrCodes.id, id), eq(qrCodes.userId, userId)))
      .returning();

    if (!updated) {
      return NextResponse.json({ error: "Not found" }, { status: 404 });
    }

    return NextResponse.json({ qrCode: updated });
  } catch (error) {
    console.error("Error updating QR code:", error);
    return NextResponse.json(
      { error: "Internal server error" },
      { status: 500 }
    );
  }
}

export async function DELETE(
  _req: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const userId = await getCurrentUserId();
    if (!userId) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const { id } = await params;

    const [deleted] = await db
      .delete(qrCodes)
      .where(and(eq(qrCodes.id, id), eq(qrCodes.userId, userId)))
      .returning();

    if (!deleted) {
      return NextResponse.json({ error: "Not found" }, { status: 404 });
    }

    return NextResponse.json({ success: true });
  } catch (error) {
    console.error("Error deleting QR code:", error);
    return NextResponse.json(
      { error: "Internal server error" },
      { status: 500 }
    );
  }
}
