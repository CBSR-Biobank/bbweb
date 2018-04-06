/**
 * Jasmine test suite
 *
 */
/* global angular */

import { TestSuiteMixin } from 'test/mixins/TestSuiteMixin';
import ngModule from '../../index'

describe('notificationsService', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, TestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              'notificationsService',
                              'toastr',
                              'Factory');

      this.user = this.Factory.user();
      spyOn(this.toastr, 'success').and.returnValue(null);
      spyOn(this.toastr, 'error').and.returnValue(null);
    });
  });

  it('submitSuccess', function() {
    this.notificationsService.submitSuccess();
    expect(this.toastr.success).toHaveBeenCalledWith('Your changes were saved.');
  });

  it('success with timeout', function() {
    var message = this.Factory.stringNext(),
        title = this.Factory.stringNext(),
        timeOut = 100,
        args;

    this.notificationsService.success(message, title, timeOut);
    expect(this.toastr.success).toHaveBeenCalled();
    args = this.toastr.success.calls.argsFor(0);
    expect(args[0]).toEqual(message);
    expect(args[1]).toEqual(title);
    expect(args[2]).toBeObject();
    expect(args[2].timeOut).toEqual(timeOut);
    expect(args[2].closeButton).toBeTrue();
    expect(args[2].extendedTimeOut).toEqual(0);
  });

  it('success with no timeout', function() {
    var message = this.Factory.stringNext(),
        title = this.Factory.stringNext(),
        args;

    this.notificationsService.success(message, title);
    expect(this.toastr.success).toHaveBeenCalled();
    args = this.toastr.success.calls.argsFor(0);
    expect(args[0]).toEqual(message);
    expect(args[1]).toEqual(title);
    expect(args[2]).toBeObject();
    expect(args[2].timeOut).toEqual(1500);
  });

  it('error with timeout', function() {
    var message = this.Factory.stringNext(),
        title = this.Factory.stringNext(),
        timeOut = 100,
        args;

    this.notificationsService.error(message, title, timeOut);
    expect(this.toastr.error).toHaveBeenCalled();
    args = this.toastr.error.calls.argsFor(0);
    expect(args[0]).toEqual(message);
    expect(args[1]).toEqual(title);
    expect(args[2]).toBeObject();
    expect(args[2].timeOut).toEqual(timeOut);
    expect(args[2].closeButton).toBeTrue();
    expect(args[2].extendedTimeOut).toEqual(timeOut * 2);
  });

  it('error with no timeout', function() {
    var message = this.Factory.stringNext(),
        title = this.Factory.stringNext(),
        args;

    this.notificationsService.error(message, title);
    expect(this.toastr.error).toHaveBeenCalled();
    args = this.toastr.error.calls.argsFor(0);
    expect(args[0]).toEqual(message);
    expect(args[1]).toEqual(title);
    expect(args[2]).toBeObject();
    expect(args[2].timeOut).toEqual(0);
    expect(args[2].extendedTimeOut).toEqual(0);
  });

  it('updateError with error message', function() {
    var err = { message: this.Factory.stringNext() },
        args;

    this.notificationsService.updateError(err);
    expect(this.toastr.error).toHaveBeenCalled();
    args = this.toastr.error.calls.argsFor(0);
    expect(args[0]).toEqual(err.message);
    expect(args[1]).toEqual('Cannot apply your change');
    expect(args[2]).toBeObject();
    expect(args[2].closeButton).toBeTrue();
    expect(args[2].timeOut).toEqual(0);
    expect(args[2].extendedTimeOut).toEqual(0);
  });

  it('updateError with no error message', function() {
    var err = {},
        args;

    this.notificationsService.updateError(err);
    expect(this.toastr.error).toHaveBeenCalled();
    args = this.toastr.error.calls.argsFor(0);
    expect(args[0]).toEqual('Your change could not be saved');
    expect(args[1]).toEqual('Cannot apply your change');
    expect(args[2]).toBeObject();
    expect(args[2].closeButton).toBeTrue();
    expect(args[2].timeOut).toEqual(0);
    expect(args[2].extendedTimeOut).toEqual(0);
  });

});
