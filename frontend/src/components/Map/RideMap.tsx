import React, { useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline, useMap, useMapEvents } from 'react-leaflet';
import L from 'leaflet';

// Custom modern SVG marker icons
const createCustomIcon = (bgColor: string, text: string) => {
  return L.divIcon({
    className: 'custom-leaflet-marker',
    html: `
      <div style="
        background-color: ${bgColor};
        color: white;
        width: 34px;
        height: 34px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 4px 10px rgba(0,0,0,0.3);
        border: 2px solid white;
        font-weight: bold;
        font-size: 13px;
        transform: translate(-17px, -17px);
      ">
        ${text}
      </div>
    `,
    iconSize: [34, 34],
    iconAnchor: [17, 17],
  });
};

const pickupIcon = createCustomIcon('#10b981', 'P');
const dropoffIcon = createCustomIcon('#ef4444', 'D');
const driverIcon = L.divIcon({
  className: 'driver-leaflet-marker',
  html: `
    <div style="
      background-color: #0f172a;
      color: #10b981;
      width: 38px;
      height: 38px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 6px 14px rgba(16,185,129,0.4);
      border: 2px solid #10b981;
      transform: translate(-19px, -19px);
      font-size: 18px;
    ">
      🚖
    </div>
  `,
  iconSize: [38, 38],
  iconAnchor: [19, 19],
});

// Auto bounds adjuster component
const FitBounds: React.FC<{ points: [number, number][] }> = ({ points }) => {
  const map = useMap();

  useEffect(() => {
    if (points.length === 1) {
      map.setView(points[0], 14, { animate: true });
    } else if (points.length > 1) {
      const bounds = L.latLngBounds(points.map((p) => L.latLng(p[0], p[1])));
      map.fitBounds(bounds, { padding: [50, 50], animate: true });
    }
  }, [map, points]);

  return null;
};

// Map click listener for interactive pickup/dropoff selection
const MapClickHandler: React.FC<{ onMapClick?: (lat: number, lng: number) => void }> = ({ onMapClick }) => {
  useMapEvents({
    click: (e) => {
      if (onMapClick) {
        onMapClick(e.latlng.lat, e.latlng.lng);
      }
    },
  });
  return null;
};

interface RideMapProps {
  pickup?: { lat: number; lng: number; label?: string };
  dropoff?: { lat: number; lng: number; label?: string };
  driverLocation?: { lat: number; lng: number; label?: string };
  onMapClick?: (lat: number, lng: number) => void;
  heightClass?: string;
}

export const RideMap: React.FC<RideMapProps> = ({
  pickup,
  dropoff,
  driverLocation,
  onMapClick,
  heightClass = 'h-[450px]',
}) => {
  const defaultCenter: [number, number] = [40.7580, -73.9855]; // New York default
  const [roadGeometry, setRoadGeometry] = React.useState<[number, number][]>([]);

  React.useEffect(() => {
    if (!pickup || !dropoff) {
      setRoadGeometry([]);
      return;
    }

    let active = true;
    const fetchRoute = async () => {
      try {
        const res = await fetch(
          `https://router.project-osrm.org/route/v1/driving/${pickup.lng},${pickup.lat};${dropoff.lng},${dropoff.lat}?overview=full&geometries=geojson`
        );
        const data = await res.json();
        if (data.routes && data.routes.length > 0 && data.routes[0].geometry) {
          const coords = data.routes[0].geometry.coordinates.map(
            (c: [number, number]) => [c[1], c[0]] as [number, number]
          );
          if (active) {
            setRoadGeometry(coords);
            return;
          }
        }
      } catch {
        // Fall back gracefully to direct straight line
      }
      if (active) {
        setRoadGeometry([
          [pickup.lat, pickup.lng],
          [dropoff.lat, dropoff.lng],
        ]);
      }
    };

    fetchRoute();
    return () => {
      active = false;
    };
  }, [pickup?.lat, pickup?.lng, dropoff?.lat, dropoff?.lng]);

  const pointsToFit: [number, number][] = [];
  if (pickup) pointsToFit.push([pickup.lat, pickup.lng]);
  if (dropoff) pointsToFit.push([dropoff.lat, dropoff.lng]);
  if (driverLocation) pointsToFit.push([driverLocation.lat, driverLocation.lng]);

  const initialCenter = pointsToFit.length > 0 ? pointsToFit[0] : defaultCenter;

  return (
    <div className={`relative w-full ${heightClass} rounded-2xl overflow-hidden shadow-inner border border-slate-200 z-0`}>
      <MapContainer
        center={initialCenter}
        zoom={13}
        scrollWheelZoom={true}
        className="h-full w-full"
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {onMapClick && <MapClickHandler onMapClick={onMapClick} />}
        {pointsToFit.length > 0 && <FitBounds points={pointsToFit} />}

        {pickup && (
          <Marker position={[pickup.lat, pickup.lng]} icon={pickupIcon}>
            <Popup>{pickup.label || 'Pickup Location'}</Popup>
          </Marker>
        )}

        {dropoff && (
          <Marker position={[dropoff.lat, dropoff.lng]} icon={dropoffIcon}>
            <Popup>{dropoff.label || 'Drop-off Location'}</Popup>
          </Marker>
        )}

        {driverLocation && (
          <Marker position={[driverLocation.lat, driverLocation.lng]} icon={driverIcon}>
            <Popup>{driverLocation.label || 'Driver Location'}</Popup>
          </Marker>
        )}

        {roadGeometry.length >= 2 && (
          <Polyline
            positions={roadGeometry}
            color="#4f46e5"
            weight={5}
            opacity={0.85}
          />
        )}
      </MapContainer>
    </div>
  );
};

export default RideMap;
