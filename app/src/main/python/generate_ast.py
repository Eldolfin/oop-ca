import os

OUTPUT_DIR = os.path.join(os.path.dirname(__file__),
                          '..', 'java/interpreteur/rouille/java/')
SYNTAX_FILE = os.path.join(os.path.dirname(__file__), 'syntax.rules')


def main():
    syntax: [str] = open(SYNTAX_FILE).readlines()

    define_ast("Expr", syntax)


def define_ast(basename: str, types: [str]):
    path = os.path.join(OUTPUT_DIR, f'{basename}.java')
    file = open(path, 'w')

    file.write(
        f'''\
package interpreteur.rouille.java;

import java.util.List;

abstract class {basename} {{''')

    define_visitor(file, basename, types)

    for tipe in types:
        classname, fields = tipe.split(':')
        classname = classname.strip()
        fields = fields.strip()
        define_type(file, basename, classname, fields)

    file.write('''
  abstract <R> R accept(Visitor<R> visitor);\n''')

    file.write('\n}')
    file.close()


def define_type(file, basename: str, classname: str, fields: str):
    file.write(f'''
  static class {classname} extends {basename} {{
    {classname}({fields}) {{''')

    # constructor boiler plate 🥱
    fields = fields.split(", ")
    for field in fields:
        name = field.split()[1]
        file.write(f'''
      this.{name} = {name};''')

    file.write('''
    }
''')

    file.write(f'''
    @Override
    <R> R accept(Visitor<R> visitor) {{
      return visitor.visit{classname}{basename}(this);
    }}
    ''')

    # fields
    for field in fields:
        file.write(f'''
    final {field};''')

    file.write('''
  }
''')


def define_visitor(file, basename: str, types: [str]):
    file.write('''
  interface Visitor<R> {\n''')

    for tipe in types:
        classname = tipe.split(':')[0].strip()
        file.write(f'''\
    R visit{classname}{basename}({classname} {basename.lower()});\n''')

    file.write('''
  }''')


if __name__ == '__main__':
    main()
