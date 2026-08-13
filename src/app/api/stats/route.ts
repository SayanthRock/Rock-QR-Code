import { NextResponse } from "next/server";
import { db } from "@/db";
import { qrCodes, scanEvents } from "@/db/schema";
import { getCurrentUserId } from "@/lib/auth";
import { eq, sql, desc } from "drizzle-orm";

export async function GET() {
  try {
    const userId = await getCurrentUserId();
    if (!userId) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const codes = await db
      .select()
      .from(qrCodes)
      .where(eq(qrCodes.userId, userId));

    const totalCodes = codes.length;
    const activeCodes = codes.filter((c) => c.isActive).length;
    const totalScans = codes.reduce((sum, c) => sum + c.scanCount, 0);

    const recentScans = await db
      .select({
        id: scanEvents.id,
        qrCodeId: scanEvents.qrCodeId,
        country: scanEvents.country,
        city: scanEvents.city,
        scannedAt: scanEvents.scannedAt,
        qrTitle: qrCodes.title,
      })
      .from(scanEvents)
      .innerJoin(qrCodes, eq(scanEvents.qrCodeId, qrCodes.id))
      .where(eq(qrCodes.userId, userId))
      .orderBy(desc(scanEvents.scannedAt))
      .limit(10);

    const topCodes = await db
      .select()
      .from(qrCodes)
      .where(eq(qrCodes.userId, userId))
      .orderBy(desc(qrCodes.scanCount))
      .limit(5);

    // Get scan counts by day for the last 7 days
    const weeklyScans = await db
      .select({
        day: sql<string>`DATE(${scanEvents.scannedAt})`.as("day"),
        count: sql<number>`COUNT(*)::int`.as("count"),
      })
      .from(scanEvents)
      .innerJoin(qrCodes, eq(scanEvents.qrCodeId, qrCodes.id))
      .where(eq(qrCodes.userId, userId))
      .groupBy(sql`DATE(${scanEvents.scannedAt})`)
      .orderBy(sql`DATE(${scanEvents.scannedAt})`);

    return NextResponse.json({
      totalCodes,
      activeCodes,
      totalScans,
      recentScans,
      topCodes,
      weeklyScans,
    });
  } catch (error) {
    console.error("Error fetching stats:", error);
    return NextResponse.json(
      { error: "Internal server error" },
      { status: 500 }
    );
  }
}
