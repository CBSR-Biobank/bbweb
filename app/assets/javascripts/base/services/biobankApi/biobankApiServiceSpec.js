/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _        from 'lodash';
import ngModule from '../../index'

describe('biobankApi service', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(TestSuiteMixin, ServerReplyMixin) {
      _.extend(this, TestSuiteMixin, ServerReplyMixin);
      this.injectDependencies('$q', '$httpBackend', 'biobankApi');
    });
  });

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation();
    this.$httpBackend.verifyNoOutstandingRequest();
  });

  it('GET method success path works', function() {
    var url = '/api/test',
        event = { test: 'test' };

    this.$httpBackend.whenGET(url).respond(this.reply(event));

    this.biobankApi.get(url).then(function(reply) {
      expect(reply).toEqual(event);
    });
    this.$httpBackend.flush();
  });

  it('GET method failure path works', function() {
    var url = '/api/test';
    var errmsg = 'test';

    this.$httpBackend.whenGET(url).respond(400, this.errorReply(errmsg));

    this.biobankApi.get(url).catch(function(err) {
      expect(err.message).toEqual(errmsg);
    });
    this.$httpBackend.flush();
  });

  it('GET method with no data returns empty object', function() {
    var promiseSuccess = false;

    this.$httpBackend.whenGET('/api/test').respond(200, undefined);
    this.biobankApi.get('/api/test').then(function(result) {
      expect(result).toEqual({});
      promiseSuccess = true;
    });
    this.$httpBackend.flush();
    expect(promiseSuccess).toBeTrue();
  });

  it('GET error response with no data returns empty object', function() {
    var promiseSuccess = false;

    this.$httpBackend.whenGET('/api/test').respond(400, undefined);
    this.biobankApi.get('/api/test').catch(function(err) {
      expect(err.data).toBeUndefined();
      expect(err.status).toEqual(400);
      promiseSuccess = true;
    });
    this.$httpBackend.flush();
    expect(promiseSuccess).toBeTrue();
  });

  it('POST method success path works', function() {
    var url = '/api/test';
    var json = { cmd: 'cmd' };
    var event = { event: 'event' };

    this.$httpBackend.expectPOST(url, json).respond(this.reply(event));

    this.biobankApi.post(url, json).then(function(reply) {
      expect(reply).toEqual(event);
    });
    this.$httpBackend.flush();
  });

  it('POST method error path works', function() {
    var url = '/api/test';
    var json = { cmd: 'cmd' };
    var errmsg = 'test';

    this.$httpBackend.expectPOST(url, json).respond(400, this.errorReply(errmsg));

    this.biobankApi.post(url, json).catch(function(err) {
      expect(err.message).toEqual(errmsg);
    });
    this.$httpBackend.flush();
  });

  it('PUT method success path works', function() {
    var url = '/api/test';
    var json = { cmd: 'cmd' };
    var event = { event: 'event' };

    this.$httpBackend.expectPUT(url, json).respond(this.reply(event));

    this.biobankApi.put(url, json).then(function(reply) {
      expect(reply).toEqual(event);
    });
    this.$httpBackend.flush();
  });

  it('PUT method error path works', function() {
    var url = '/api/test';
    var cmd = { cmd: 'cmd' };
    var errmsg = 'test';

    this.$httpBackend.expectPUT(url, cmd).respond(400, this.errorReply(errmsg));

    this.biobankApi.put(url, cmd).catch(function(err) {
      expect(err.message).toEqual(errmsg);
    });
    this.$httpBackend.flush();
  });

  it('DELETE method success path works', function() {
    var url = '/api/test';
    var event = { test: 'test' };

    this.$httpBackend.whenDELETE(url).respond(this.reply(event));

    this.biobankApi.del(url).then(function(reply) {
      expect(reply).toEqual(event);
    });
    this.$httpBackend.flush();
  });

  it('DELETE method failure path works', function() {
    var url = '/api/test';
    var errmsg = 'test';

    this.$httpBackend.whenDELETE(url).respond(400, this.errorReply(errmsg));

    this.biobankApi.del(url).catch(function(err) {
      expect(err.message).toEqual(errmsg);
    });
    this.$httpBackend.flush();
  });

});
