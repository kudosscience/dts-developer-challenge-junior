import { app } from '../../main/app';
import { expect } from 'chai';
import request from 'supertest';
import nock from 'nock';

const BACKEND_URL = 'http://localhost:4000';

describe('Tasks routes', () => {
  afterEach(() => {
    nock.cleanAll();
  });

  describe('GET /tasks/create', () => {
    test('should return the task creation form', async () => {
      await request(app)
        .get('/tasks/create')
        .expect(res => expect(res.status).to.equal(200))
        .expect(res => expect(res.text).to.include('Create a New Task'));
    });

    test('should include form fields for task creation', async () => {
      const response = await request(app).get('/tasks/create');
      
      expect(response.text).to.include('id="title"');
      expect(response.text).to.include('id="description"');
      expect(response.text).to.include('id="status"');
      expect(response.text).to.include('id="due-date-day"');
      expect(response.text).to.include('id="due-date-month"');
      expect(response.text).to.include('id="due-date-year"');
    });
  });

  describe('POST /tasks/create', () => {
    test('should show validation error when title is missing', async () => {
      const response = await request(app)
        .post('/tasks/create')
        .send({
          status: 'PENDING',
          dueDateDay: '25',
          dueDateMonth: '12',
          dueDateYear: '2025',
          dueDateHour: '17',
          dueDateMinute: '00'
        });

      expect(response.status).to.equal(200);
      expect(response.text).to.include('Enter a task title');
    });

    test('should show validation error when status is missing', async () => {
      const response = await request(app)
        .post('/tasks/create')
        .send({
          title: 'Test Task',
          dueDateDay: '25',
          dueDateMonth: '12',
          dueDateYear: '2025',
          dueDateHour: '17',
          dueDateMinute: '00'
        });

      expect(response.status).to.equal(200);
      expect(response.text).to.include('Select a task status');
    });

    test('should show validation error when due date is incomplete', async () => {
      const response = await request(app)
        .post('/tasks/create')
        .send({
          title: 'Test Task',
          status: 'PENDING',
          dueDateDay: '',
          dueDateMonth: '12',
          dueDateYear: '2025'
        });

      expect(response.status).to.equal(200);
      expect(response.text).to.include('Enter a complete due date');
    });

    test('should show validation error when due date is in the past', async () => {
      const response = await request(app)
        .post('/tasks/create')
        .send({
          title: 'Test Task',
          status: 'PENDING',
          dueDateDay: '01',
          dueDateMonth: '01',
          dueDateYear: '2020',
          dueDateHour: '00',
          dueDateMinute: '00'
        });

      expect(response.status).to.equal(200);
      expect(response.text).to.include('Due date must be in the future');
    });

    test('should create task successfully with valid data', async () => {
      // Mock the backend API
      const mockTask = {
        id: 1,
        title: 'Test Task',
        description: 'Test Description',
        status: 'PENDING',
        dueDate: '2025-12-25T17:00:00',
        createdAt: '2025-12-06T10:00:00',
        updatedAt: '2025-12-06T10:00:00'
      };

      nock(BACKEND_URL)
        .post('/api/tasks')
        .reply(201, mockTask);

      const response = await request(app)
        .post('/tasks/create')
        .send({
          title: 'Test Task',
          description: 'Test Description',
          status: 'PENDING',
          dueDateDay: '25',
          dueDateMonth: '12',
          dueDateYear: '2025',
          dueDateHour: '17',
          dueDateMinute: '00'
        });

      expect(response.status).to.equal(200);
      expect(response.text).to.include('Task created successfully');
      expect(response.text).to.include('Test Task');
    });

    test('should create task without optional description', async () => {
      const mockTask = {
        id: 2,
        title: 'Task Without Description',
        description: null,
        status: 'IN_PROGRESS',
        dueDate: '2025-12-31T23:59:00',
        createdAt: '2025-12-06T10:00:00',
        updatedAt: '2025-12-06T10:00:00'
      };

      nock(BACKEND_URL)
        .post('/api/tasks')
        .reply(201, mockTask);

      const response = await request(app)
        .post('/tasks/create')
        .send({
          title: 'Task Without Description',
          status: 'IN_PROGRESS',
          dueDateDay: '31',
          dueDateMonth: '12',
          dueDateYear: '2025',
          dueDateHour: '23',
          dueDateMinute: '59'
        });

      expect(response.status).to.equal(200);
      expect(response.text).to.include('Task created successfully');
    });

    test('should handle backend API errors gracefully', async () => {
      nock(BACKEND_URL)
        .post('/api/tasks')
        .reply(400, {
          status: 400,
          message: 'Validation failed',
          errors: ['title: Title is required']
        });

      const response = await request(app)
        .post('/tasks/create')
        .send({
          title: 'Test Task',
          status: 'PENDING',
          dueDateDay: '25',
          dueDateMonth: '12',
          dueDateYear: '2025',
          dueDateHour: '17',
          dueDateMinute: '00'
        });

      expect(response.status).to.equal(200);
      expect(response.text).to.include('title: Title is required');
    });

    test('should handle network errors gracefully', async () => {
      nock(BACKEND_URL)
        .post('/api/tasks')
        .replyWithError('Network error');

      const response = await request(app)
        .post('/tasks/create')
        .send({
          title: 'Test Task',
          status: 'PENDING',
          dueDateDay: '25',
          dueDateMonth: '12',
          dueDateYear: '2025',
          dueDateHour: '17',
          dueDateMinute: '00'
        });

      expect(response.status).to.equal(200);
      expect(response.text).to.include('Failed to create task');
    });
  });
});
