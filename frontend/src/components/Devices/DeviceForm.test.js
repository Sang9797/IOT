import React from 'react';
import { fireEvent, render, screen } from '@testing-library/react';
import DeviceForm from './DeviceForm';

describe('DeviceForm', () => {
  it('renders broker selector options', () => {
    render(
      <DeviceForm
        isOpen
        onClose={() => {}}
        onSubmit={() => {}}
        title="Create Device"
        brokers={[{ id: 'broker-1', name: 'Broker 1', protocol: 'tcp', host: 'host-1', port: 1883 }]}
      />
    );

    expect(screen.getByLabelText(/mqtt broker/i)).toBeInTheDocument();
    expect(screen.getByRole('option', { name: /Broker 1/ })).toBeInTheDocument();
  });

  it('requires broker selection before submit', async () => {
    render(<DeviceForm isOpen onClose={() => {}} onSubmit={() => {}} title="Create Device" brokers={[]} />);

    fireEvent.change(screen.getByPlaceholderText(/Enter device name/i), { target: { value: 'Device 1' } });
    fireEvent.change(screen.getByPlaceholderText(/192.168.1.100/i), { target: { value: '192.168.1.10' } });
    fireEvent.click(screen.getByRole('button', { name: /create device/i }));

    expect(await screen.findByText(/MQTT broker is required/i)).toBeInTheDocument();
  });
});
