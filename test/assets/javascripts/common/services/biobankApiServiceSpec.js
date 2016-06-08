/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('biobankApi service', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(serverReplyMixin) {
      _.extend(this, serverReplyMixin);

      this.$q           = this.$injector.get('$q');
      this.$httpBackend = this.$injector.get('$httpBackend');
      this.biobankApi   = this.$injector.get('biobankApi');
    }));

    afterEach(function() {
      this.$httpBackend.verifyNoOutstandingExpectation();
      this.$httpBackend.verifyNoOutstandingRequest();
    });

    it('GET method success path works', function() {
      var url = '/test',
          event = { test: 'test' };

      this.$httpBackend.whenGET(url).respond(this.reply(event));

      this.biobankApi.get(url).then(function(reply) {
        expect(reply).toEqual(event);
      });
      this.$httpBackend.flush();
    });

    it('GET method failure path works', function() {
      var url = '/test';
      var errmsg = 'test';

      this.$httpBackend.whenGET(url).respond(400, this.errorReply(errmsg));

      this.biobankApi.get(url).catch(function(err) {
        expect(err.data.message).toEqual(errmsg);
      });
      this.$httpBackend.flush();
    });

    it('POST method success path works', function() {
      var url = '/test';
      var json = { cmd: 'cmd' };
      var event = { event: 'event' };

      this.$httpBackend.expectPOST(url, json).respond(this.reply(event));

      this.biobankApi.post(url, json).then(function(reply) {
        expect(reply).toEqual(event);
      });
      this.$httpBackend.flush();
    });

    it('POST method error path works', function() {
      var url = '/test';
      var json = { cmd: 'cmd' };
      var errmsg = 'test';

      this.$httpBackend.expectPOST(url, json).respond(400, this.errorReply(errmsg));

      this.biobankApi.post(url, json).catch(function(err) {
        expect(err.data.message).toEqual(errmsg);
      });
      this.$httpBackend.flush();
    });

    it('PUT method success path works', function() {
      var url = '/test';
      var json = { cmd: 'cmd' };
      var event = { event: 'event' };

      this.$httpBackend.expectPUT(url, json).respond(this.reply(event));

      this.biobankApi.put(url, json).then(function(reply) {
        expect(reply).toEqual(event);
      });
      this.$httpBackend.flush();
    });

    it('PUT method error path works', function() {
      var url = '/test';
      var cmd = { cmd: 'cmd' };
      var errmsg = 'test';

      this.$httpBackend.expectPUT(url, cmd).respond(400, this.errorReply(errmsg));

      this.biobankApi.put(url, cmd).catch(function(err) {
        expect(err.data.message).toEqual(errmsg);
      });
      this.$httpBackend.flush();
    });

    it('DELETE method success path works', function() {
      var url = '/test';
      var event = { test: 'test' };

      this.$httpBackend.whenDELETE(url).respond(this.reply(event));

      this.biobankApi.del(url).then(function(reply) {
        expect(reply.data).toEqual(event);
      });
      this.$httpBackend.flush();
    });

    it('DELETE method failure path works', function() {
      var url = '/test';
      var errmsg = 'test';

      this.$httpBackend.whenDELETE(url).respond(400, this.errorReply(errmsg));

      this.biobankApi.del(url).catch(function(err) {
        expect(err.data.message).toEqual(errmsg);
      });
      this.$httpBackend.flush();
    });

  });

});
