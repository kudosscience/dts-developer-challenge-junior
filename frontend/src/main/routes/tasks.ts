import { Application, Request, Response } from 'express';
import axios from 'axios';

// Helper to get CSRF token safely
const getCsrfToken = (req: Request): string => {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  return (req as any).csrfToken?.() || '';
};

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:4000';

interface TaskFormData {
  title: string;
  description?: string;
  status: string;
  dueDateDay: string;
  dueDateMonth: string;
  dueDateYear: string;
  dueDateHour: string;
  dueDateMinute: string;
}

interface ValidationError {
  text: string;
  href: string;
}

interface TaskResponse {
  id: number;
  title: string;
  description: string | null;
  status: string;
  dueDate: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Validates the task form data and returns any errors found.
 */
function validateTaskForm(data: TaskFormData): { 
  errors: ValidationError[]; 
  titleError?: string;
  statusError?: string;
  dueDateError?: string;
} {
  const errors: ValidationError[] = [];
  let titleError: string | undefined;
  let statusError: string | undefined;
  let dueDateError: string | undefined;

  // Validate title
  if (!data.title || data.title.trim() === '') {
    titleError = 'Enter a task title';
    errors.push({ text: titleError, href: '#title' });
  } else if (data.title.length > 255) {
    titleError = 'Task title must be 255 characters or fewer';
    errors.push({ text: titleError, href: '#title' });
  }

  // Validate status
  const validStatuses = ['PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'];
  if (!data.status || !validStatuses.includes(data.status)) {
    statusError = 'Select a task status';
    errors.push({ text: statusError, href: '#status' });
  }

  // Validate due date
  const { dueDateDay, dueDateMonth, dueDateYear, dueDateHour, dueDateMinute } = data;
  
  if (!dueDateDay || !dueDateMonth || !dueDateYear) {
    dueDateError = 'Enter a complete due date';
    errors.push({ text: dueDateError, href: '#due-date-day' });
  } else {
    const day = Number.parseInt(dueDateDay, 10);
    const month = Number.parseInt(dueDateMonth, 10);
    const year = Number.parseInt(dueDateYear, 10);
    const hour = Number.parseInt(dueDateHour || '0', 10);
    const minute = Number.parseInt(dueDateMinute || '0', 10);

    if (Number.isNaN(day) || Number.isNaN(month) || Number.isNaN(year) || day < 1 || day > 31 || month < 1 || month > 12) {
      dueDateError = 'Enter a valid date';
      errors.push({ text: dueDateError, href: '#due-date-day' });
    } else if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
      dueDateError = 'Enter a valid time';
      errors.push({ text: dueDateError, href: '#due-date-hour' });
    } else {
      // Check if date is in the future
      const dueDate = new Date(year, month - 1, day, hour, minute);
      if (dueDate <= new Date()) {
        dueDateError = 'Due date must be in the future';
        errors.push({ text: dueDateError, href: '#due-date-day' });
      }
    }
  }

  return { errors, titleError, statusError, dueDateError };
}

/**
 * Formats an ISO date string for display.
 */
function formatDateTime(isoString: string): string {
  const date = new Date(isoString);
  return date.toLocaleString('en-GB', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

/**
 * Converts form data to ISO date string.
 */
function buildIsoDateTime(data: TaskFormData): string {
  const year = data.dueDateYear;
  const month = data.dueDateMonth.padStart(2, '0');
  const day = data.dueDateDay.padStart(2, '0');
  const hour = (data.dueDateHour || '00').padStart(2, '0');
  const minute = (data.dueDateMinute || '00').padStart(2, '0');
  return `${year}-${month}-${day}T${hour}:${minute}:00`;
}

export default function (app: Application): void {
  /**
   * GET /tasks/create - Display the task creation form
   */
  app.get('/tasks/create', (req: Request, res: Response) => {
    res.render('create-task', {
      csrfToken: getCsrfToken(req)
    });
  });

  /**
   * POST /tasks/create - Handle task creation form submission
   */
  app.post('/tasks/create', async (req: Request, res: Response) => {
    const formData: TaskFormData = {
      title: req.body.title?.trim() || '',
      description: req.body.description?.trim() || undefined,
      status: req.body.status || '',
      dueDateDay: req.body.dueDateDay?.trim() || '',
      dueDateMonth: req.body.dueDateMonth?.trim() || '',
      dueDateYear: req.body.dueDateYear?.trim() || '',
      dueDateHour: req.body.dueDateHour?.trim() || '',
      dueDateMinute: req.body.dueDateMinute?.trim() || ''
    };

    // Validate form data
    const { errors, titleError, statusError, dueDateError } = validateTaskForm(formData);

    if (errors.length > 0) {
      return res.render('create-task', {
        csrfToken: getCsrfToken(req),
        errors,
        titleError,
        statusError,
        dueDateError,
        formData
      });
    }

    try {
      // Build the request payload for the backend API
      const payload = {
        title: formData.title,
        description: formData.description || null,
        status: formData.status,
        dueDate: buildIsoDateTime(formData)
      };

      // Call the backend API
      const response = await axios.post<TaskResponse>(`${BACKEND_URL}/api/tasks`, payload, {
        headers: {
          'Content-Type': 'application/json'
        }
      });

      const createdTask = response.data;

      // Render success page with task details
      res.render('create-task', {
        csrfToken: getCsrfToken(req),
        success: true,
        createdTask: {
          ...createdTask,
          formattedDueDate: formatDateTime(createdTask.dueDate),
          formattedCreatedAt: formatDateTime(createdTask.createdAt)
        }
      });
    } catch (error) {
      console.error('Error creating task:', error);
      
      // Handle API validation errors
      let apiErrors: ValidationError[] = [];
      if (axios.isAxiosError(error) && error.response?.data?.errors) {
        apiErrors = error.response.data.errors.map((err: string) => ({
          text: err,
          href: '#title'
        }));
      } else {
        apiErrors = [{ text: 'Failed to create task. Please try again.', href: '#title' }];
      }

      res.render('create-task', {
        csrfToken: getCsrfToken(req),
        errors: apiErrors,
        formData
      });
    }
  });
}
