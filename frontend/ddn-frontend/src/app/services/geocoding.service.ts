import { Injectable } from '@angular/core';


export type LatLng = { lat: number; lng: number };
export type Suggestion = { label: string; lat: number; lng: number };


@Injectable({ providedIn: 'root' })
export class GeocodingService {


// Novi Sad bounding box (left,top,right,bottom)
private bbox = '19.74,45.34,20.06,45.20';


async suggest(query: string, limit = 6): Promise<Suggestion[]> {
const q = query.trim();
if (!q) return [];


const url =
`https://photon.komoot.io/api/?q=${encodeURIComponent(q)}&limit=${limit}&bbox=${this.bbox}`;


const res = await fetch(url, { headers: { Accept: 'application/json' } });
if (!res.ok) return [];


const data = await res.json();
const features = data?.features ?? [];


return features.map((f: any) => {
const coords = f?.geometry?.coordinates; // [lon, lat]
const p = f?.properties ?? {};


const label =
p.name
? `${p.name}${p.street ? ', ' + p.street : ''}${p.housenumber ? ' ' + p.housenumber : ''}${p.city ? ', ' + p.city : ''}`
: (p.label ?? '');


if (!coords) return null;


return {
label: label || p.label || 'Unknown',
lng: Number(coords[0]),
lat: Number(coords[1]),
};
}).filter(Boolean);
}


async geocode(query: string): Promise<LatLng | null> {
const results = await this.suggest(query, 1);
if (!results.length) return null;
return { lat: results[0].lat, lng: results[0].lng };
}
}