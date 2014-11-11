require 'docs'
require 'coderay'

Awestruct::Extensions::Pipeline.new do
  extension Awestruct::Extensions::Docs::Index.new('/docs', :docs)

  # extension Awestruct::Extensions::Posts.new '/news'
  # extension Awestruct::Extensions::Indexifier.new
  # Indexifier *must* come before Atomizer
  # extension Awestruct::Extensions::Atomizer.new :posts, '/feed.atom'
end