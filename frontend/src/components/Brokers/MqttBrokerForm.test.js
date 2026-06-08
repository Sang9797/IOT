import React from 'react';
import { fireEvent, render, screen } from '@testing-library/react';
import MqttBrokerForm from './MqttBrokerForm';

describe('MqttBrokerForm', () => {
  it('validates required broker fields', async () => {
    render(<MqttBrokerForm isOpen onClose={() => {}} onSubmit={() => {}} title="Create MQTT Broker" />);

    fireEvent.click(screen.getByRole('button', { name: /create broker/i }));

    expect(await screen.findByText(/Broker name is required/i)).toBeInTheDocument();
    expect(await screen.findByText(/Broker host is required/i)).toBeInTheDocument();
  });
});
