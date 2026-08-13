import { NextRequest, NextResponse } from "next/server";
import QRCode from "qrcode";

export async function POST(req: NextRequest) {
  try {
    const { targetUrl, fgColor = "#000000", bgColor = "#FFFFFF", size = 300 } = await req.json();
    if (!targetUrl) {
      return NextResponse.json({ error: "URL required" }, { status: 400 });
    }

    const dataUrl = await QRCode.toDataURL(targetUrl, {
      width: size,
      color: { dark: fgColor, light: bgColor },
      margin: 2,
      errorCorrectionLevel: "H",
    });

    return NextResponse.json({ dataUrl });
  } catch (error) {
    console.error("Preview error:", error);
    return NextResponse.json({ error: "Failed to generate preview" }, { status: 500 });
  }
}
