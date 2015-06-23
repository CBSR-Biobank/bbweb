/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('biobankApi service', function() {

    var httpBackend, biobankApi;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($httpBackend, _biobankApi_) {
      httpBackend = $httpBackend;
      biobankApi  = _biobankApi_;
    }));

    it('GET method success path works', function(done) {
      var url = '/test';
      var event = { test: 'test' };

      httpBackend.whenGET(url).respond(201, serverReply(event));

      biobankApi.get(url).then(function(reply) {
        expect(reply).toEqual(event);
        done();
      });
      httpBackend.flush();
    });

    it('GET method failure path works', function(done) {
      var url = '/test';
      var errmsg = 'test';

      httpBackend.whenGET(url).respond(401, serverErrReply(errmsg));

      biobankApi.get(url).catch(function(err) {
        expect(err.data.message).toEqual(errmsg);
        done();
      });
      httpBackend.flush();
    });

    it('POST method success path works', function(done) {
      var url = '/test';
      var cmd = { cmd: 'cmd' };
      var event = { event: 'event' };

      httpBackend.expectPOST(url, cmd).respond(201, serverReply(event));

      biobankApi.post(url, cmd).then(function(reply) {
        expect(reply).toEqual(event);
        done();
      });
      httpBackend.flush();
    });

    it('POST method error path works', function(done) {
      var url = '/test';
      var cmd = { cmd: 'cmd' };
      var errmsg = 'test';

      httpBackend.expectPOST(url, cmd).respond(401, serverErrReply(errmsg));

      biobankApi.post(url, cmd).catch(function(err) {
        expect(err.data.message).toEqual(errmsg);
        done();
      });
      httpBackend.flush();
    });

    it('PUT method success path works', function(done) {
      var url = '/test';
      var cmd = { cmd: 'cmd' };
      var event = { event: 'event' };

      httpBackend.expectPUT(url, cmd).respond(201, serverReply(event));

      biobankApi.put(url, cmd).then(function(reply) {
        expect(reply).toEqual(event);
        done();
      });
      httpBackend.flush();
    });

    it('PUT method error path works', function(done) {
      var url = '/test';
      var cmd = { cmd: 'cmd' };
      var errmsg = 'test';

      httpBackend.expectPUT(url, cmd).respond(401, serverErrReply(errmsg));

      biobankApi.put(url, cmd).catch(function(err) {
        expect(err.data.message).toEqual(errmsg);
        done();
      });
      httpBackend.flush();
    });

    it('DELETE method success path works', function(done) {
      var url = '/test';
      var event = { test: 'test' };

      httpBackend.whenDELETE(url).respond(201, serverReply(event));

      biobankApi.del(url).then(function(reply) {
        expect(reply.data).toEqual(event);
        done();
      });
      httpBackend.flush();
    });

    it('DELETE method failure path works', function(done) {
      var url = '/test';
      var errmsg = 'test';

      httpBackend.whenDELETE(url).respond(401, serverErrReply(errmsg));

      biobankApi.del(url).catch(function(err) {
        expect(err.data.message).toEqual(errmsg);
        done();
      });
      httpBackend.flush();
    });

    function serverErrReply(message) {
      return { status: 'error', message: message };
    }

    function serverReply(event) {
      return { status: 'error', data: event };
    }

  });

});
